package com.turkce.kelimesolitaire.data.billing

import android.app.Activity
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.util.Log
import com.turkce.kelimesolitaire.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class InAppBillingManager private constructor() {

    companion object {
        const val TAG = "InAppBillingManager"
        const val RC_BUY = 10001

        @Volatile
        private var instance: InAppBillingManager? = null

        fun getInstance(): InAppBillingManager {
            return instance ?: synchronized(this) {
                instance ?: InAppBillingManager().also { instance = it }
            }
        }
    }

    private var billingService: InAppBillingProxy? = null
    private var isBound = false
    private var activeMarketPackage = MyketBillingConfig.MYKET_BILLING_PACKAGE
    private var activeBindAction = MyketBillingConfig.MYKET_BIND_ACTION
    private var activePublicKey = MyketBillingConfig.MYKET_RSA_PUBLIC_KEY
    private var activeDescriptor = "com.android.vending.billing.IInAppBillingService"

    private var appContext: Context? = null
    private var onPurchasesRestoredCallback: ((Set<String>) -> Unit)? = null

    /** SKU of the purchase currently in progress (null when nothing is pending). */
    @Volatile
    private var pendingSku: String? = null

    val isConnected: Boolean
        get() = billingService != null

    /** Result of a purchase that passed every local check. */
    private data class VerifiedPurchase(
        val sku: String,
        val token: String,
        val packageName: String
    )

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Billing service connected")
            if (service != null) {
                val remoteDesc = try {
                    service.interfaceDescriptor?.takeIf { it.isNotBlank() } ?: activeDescriptor
                } catch (e: Exception) {
                    activeDescriptor
                }
                billingService = InAppBillingProxy(service, remoteDesc)
                queryAndRestorePurchases()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Billing service disconnected")
            billingService = null
            isBound = false
        }
    }

    fun initialize(context: Context, onPurchasesRestored: ((Set<String>) -> Unit)? = null) {
        val appCtx = context.applicationContext
        this.appContext = appCtx
        if (onPurchasesRestored != null) {
            this.onPurchasesRestoredCallback = onPurchasesRestored
        }
        setupTargetMarket(appCtx)

        if (isBound && billingService != null) {
            queryAndRestorePurchases()
            return
        }

        try {
            val intent = Intent(activeBindAction).apply {
                `package` = activeMarketPackage
            }
            isBound = appCtx.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            Log.d(TAG, "Binding to $activeMarketPackage returned $isBound")
        } catch (e: Exception) {
            Log.e(TAG, "Error binding billing service: ${e.message}")
        }
    }

    private fun setupTargetMarket(context: Context) {
        val isMyketInstalled = isPackageInstalled(context, MyketBillingConfig.MYKET_BILLING_PACKAGE)
        val isBazaarInstalled = isPackageInstalled(context, "com.farsitel.bazaar")

        val flavor = try { BuildConfig.FLAVOR } catch (e: Throwable) { "" }

        if (flavor.equals("bazaar", ignoreCase = true)) {
            activeMarketPackage = "com.farsitel.bazaar"
            activeBindAction = "ir.cafebazaar.pardakht.InAppBillingService.BIND"
            activePublicKey = BazaarBillingConfig.BAZAAR_RSA_PUBLIC_KEY
        } else {
            activeMarketPackage = MyketBillingConfig.MYKET_BILLING_PACKAGE
            activeBindAction = MyketBillingConfig.MYKET_BIND_ACTION
            activePublicKey = MyketBillingConfig.MYKET_RSA_PUBLIC_KEY
        }
        activeDescriptor = "com.android.vending.billing.IInAppBillingService"
    }

    fun isMarketInstalled(context: Context): Boolean {
        return isPackageInstalled(context, activeMarketPackage)
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // ------------------------------------------------------------------
    // Purchase flow
    // ------------------------------------------------------------------

    fun startPurchase(
        activity: Activity,
        sku: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isKnownSku(sku)) {
            onError("محصول نامعتبر است.")
            return
        }

        if (!isMarketInstalled(activity)) {
            val marketName = if (activeMarketPackage.contains("bazaar")) "کافه بازار" else "مایکت"
            onError("برنامه $marketName روی گوشی شما نصب نیست. لطفاً ابتدا آن را نصب کنید.")
            return
        }

        val service = billingService
        if (service == null) {
            initialize(activity)
            onError("در حال اتصال به مارکت... لطفاً چند ثانیه دیگر دوباره امتحان کنید.")
            return
        }

        pendingSku = sku

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val packageName = activity.packageName
                val developerPayload = "payload_${System.currentTimeMillis()}"
                val buyBundle = service.getBuyIntent(3, packageName, sku, "inapp", developerPayload)

                when (val responseCode = buyBundle.getInt("RESPONSE_CODE", -1)) {
                    0 -> {
                        val buyIntent = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            buyBundle.getParcelable("BUY_INTENT", PendingIntent::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            buyBundle.getParcelable("BUY_INTENT")
                        }

                        if (buyIntent != null) {
                            withContext(Dispatchers.Main) {
                                try {
                                    activity.startIntentSenderForResult(
                                        buyIntent.intentSender,
                                        RC_BUY,
                                        Intent(),
                                        0,
                                        0,
                                        0
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error starting intent sender")
                                    pendingSku = null
                                    onError("خطا در باز کردن درگاه پرداخت.")
                                }
                            }
                        } else {
                            pendingSku = null
                            withContext(Dispatchers.Main) {
                                onError("خطا در دریافت درگاه پرداخت مارکت.")
                            }
                        }
                    }

                    7 -> {
                        // Item already owned
                        pendingSku = null
                        if (isSkuConsumable(sku)) {
                            // Stale unconsumed purchase: clear it and ask the user to buy again
                            consumeOwnedSku(service, activity, sku)
                            withContext(Dispatchers.Main) {
                                onError("خرید قبلی پاک شد. لطفاً دوباره تلاش کنید.")
                            }
                        } else {
                            // Non-consumable already owned: RESTORE ONLY. Never grant coins/items again.
                            queryAndRestorePurchases()
                            withContext(Dispatchers.Main) {
                                onError("این محصول را قبلاً خریده‌اید و بازیابی شد.")
                            }
                        }
                    }

                    else -> {
                        pendingSku = null
                        withContext(Dispatchers.Main) {
                            onError("خطای پرداخت مارکت (کد: $responseCode)")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting purchase")
                pendingSku = null
                withContext(Dispatchers.Main) {
                    onError("خطا در برقراری ارتباط با مارکت.")
                }
            }
        }
    }

    /**
     * Must be called from Activity.onActivityResult.
     * Order of operations: verify -> grant (onSuccess) -> consume.
     */
    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        if (requestCode != RC_BUY) return false

        val expectedSku = pendingSku
        pendingSku = null

        if (resultCode != Activity.RESULT_OK || data == null) {
            onError("پرداخت لغو شد.")
            return true
        }

        val responseCode = data.getIntExtra("RESPONSE_CODE", 0)
        if (responseCode != 0) {
            onError("خطای پرداخت مارکت (کد: $responseCode)")
            return true
        }

        val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
        val dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE")

        if (purchaseData.isNullOrEmpty()) {
            onError("اطلاعات خرید دریافت نشد.")
            return true
        }

        val purchase = verifyPurchase(purchaseData, dataSignature)
        if (purchase == null) {
            onError("خرید نامعتبر است.")
            return true
        }

        // If we know which SKU we started, the result must match it.
        // (expectedSku can be null only if the process was killed while the payment screen was open.)
        if (expectedSku != null && purchase.sku != expectedSku) {
            Log.e(TAG, "SKU mismatch between request and result")
            onError("خرید نامعتبر است.")
            return true
        }

        // 1) Grant first (the caller persists coins/flags)
        onSuccess(purchase.sku)

        // 2) Consume afterwards, so a crash can never lose a paid item
        if (isSkuConsumable(purchase.sku) && purchase.token.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    billingService?.consumePurchase(3, purchase.packageName, purchase.token)
                } catch (e: Throwable) {
                    Log.e(TAG, "Error consuming purchase")
                }
            }
        }

        return true
    }

    /**
     * Fail-closed verification: a purchase is accepted only if
     * the key exists, the RSA signature is valid, state is "purchased",
     * the package name is ours and the SKU is one we sell.
     */
    private fun verifyPurchase(purchaseData: String, signature: String?): VerifiedPurchase? {
        if (activePublicKey.isBlank() || signature.isNullOrEmpty()) return null
        if (!Security.verifyPurchase(activePublicKey, purchaseData, signature)) return null

        return try {
            val json = JSONObject(purchaseData)
            val sku = json.optString("productId")
            val pkg = json.optString("packageName")

            if (json.optInt("purchaseState", 0) != 0) return null
            if (pkg.isEmpty() || pkg != appContext?.packageName) return null
            if (!isKnownSku(sku)) return null

            VerifiedPurchase(sku, json.optString("purchaseToken"), pkg)
        } catch (e: Exception) {
            null
        }
    }

    // ------------------------------------------------------------------
    // Restore
    // ------------------------------------------------------------------

    private fun queryAndRestorePurchases() {
        val service = billingService ?: return
        val ctx = appContext ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ownedSkus = mutableSetOf<String>()
                var continuationToken: String? = null
                do {
                    val bundle = service.getPurchases(3, ctx.packageName, "inapp", continuationToken)
                    val responseCode = bundle.getInt("RESPONSE_CODE", -1)
                    if (responseCode != 0) break

                    val dataList = bundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST")
                    val sigList = bundle.getStringArrayList("INAPP_DATA_SIGNATURE_LIST")
                    if (dataList != null && sigList != null) {
                        for (i in dataList.indices) {
                            // Only signature-verified purchases are restored
                            val verified = verifyPurchase(dataList[i], sigList.getOrNull(i))
                            if (verified != null) ownedSkus.add(verified.sku)
                        }
                    }
                    continuationToken = bundle.getString("INAPP_CONTINUATION_TOKEN")
                } while (!continuationToken.isNullOrEmpty())

                if (ownedSkus.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        onPurchasesRestoredCallback?.invoke(ownedSkus)
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error querying purchases")
            }
        }
    }

    private fun consumeOwnedSku(service: InAppBillingProxy, ctx: Context, sku: String) {
        try {
            val bundle = service.getPurchases(3, ctx.packageName, "inapp", null)
            if (bundle.getInt("RESPONSE_CODE", -1) != 0) return
            val items = bundle.getStringArrayList("INAPP_PURCHASE_ITEM_LIST") ?: return
            val datas = bundle.getStringArrayList("INAPP_PURCHASE_DATA_LIST") ?: return
            val idx = items.indexOf(sku)
            if (idx == -1) return
            val token = JSONObject(datas[idx]).optString("purchaseToken")
            if (token.isNotEmpty()) service.consumePurchase(3, ctx.packageName, token)
        } catch (e: Throwable) {
            Log.e(TAG, "consumeOwnedSku failed")
        }
    }

    // ------------------------------------------------------------------
    // SKU helpers
    // ------------------------------------------------------------------

    private fun isKnownSku(sku: String): Boolean {
        return MyketBillingConfig.ALL_PRODUCTS.any { it.sku == sku }
    }

    private fun isSkuConsumable(sku: String): Boolean {
        return when (sku) {
            MyketBillingConfig.SKU_REMOVE_ADS,
            MyketBillingConfig.SKU_STARTER_PACK -> false
            else -> true
        }
    }

    fun unbind(context: Context) {
        if (isBound) {
            try {
                context.applicationContext.unbindService(serviceConnection)
            } catch (e: Exception) {
                Log.e(TAG, "Error unbinding service")
            }
            isBound = false
            billingService = null
        }
    }
}

/**
 * Pure Kotlin IPC Proxy for standard In-App Billing V3 (Myket and Cafe Bazaar).
 * Works without AIDL compiler dependency.
 */
class InAppBillingProxy(
    private val remote: IBinder,
    private val descriptor: String = "com.android.vending.billing.IInAppBillingService"
) : IInterface {

    private val effectiveDescriptor: String
        get() = try {
            val queried = remote.interfaceDescriptor
            if (!queried.isNullOrBlank()) queried else descriptor
        } catch (_: Throwable) {
            descriptor
        }

    override fun asBinder(): IBinder = remote

    fun isBillingSupported(apiVersion: Int, packageName: String, type: String): Int {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(effectiveDescriptor)
            data.writeInt(apiVersion)
            data.writeString(packageName)
            data.writeString(type)
            remote.transact(IBinder.FIRST_CALL_TRANSACTION + 0, data, reply, 0)
            reply.readException()
            reply.readInt()
        } finally {
            reply.recycle()
            data.recycle()
        }
    }

    fun getBuyIntent(
        apiVersion: Int,
        packageName: String,
        sku: String,
        type: String,
        developerPayload: String?
    ): Bundle {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(effectiveDescriptor)
            data.writeInt(apiVersion)
            data.writeString(packageName)
            data.writeString(sku)
            data.writeString(type)
            data.writeString(developerPayload)
            remote.transact(IBinder.FIRST_CALL_TRANSACTION + 2, data, reply, 0)
            reply.readException()
            if (reply.readInt() != 0) {
                Bundle.CREATOR.createFromParcel(reply)
            } else {
                Bundle()
            }
        } finally {
            reply.recycle()
            data.recycle()
        }
    }

    fun getPurchases(
        apiVersion: Int,
        packageName: String,
        type: String,
        continuationToken: String?
    ): Bundle {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(effectiveDescriptor)
            data.writeInt(apiVersion)
            data.writeString(packageName)
            data.writeString(type)
            data.writeString(continuationToken)
            remote.transact(IBinder.FIRST_CALL_TRANSACTION + 3, data, reply, 0)
            reply.readException()
            if (reply.readInt() != 0) {
                Bundle.CREATOR.createFromParcel(reply)
            } else {
                Bundle()
            }
        } finally {
            reply.recycle()
            data.recycle()
        }
    }

    fun consumePurchase(apiVersion: Int, packageName: String, purchaseToken: String): Int {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(effectiveDescriptor)
            data.writeInt(apiVersion)
            data.writeString(packageName)
            data.writeString(purchaseToken)
            remote.transact(IBinder.FIRST_CALL_TRANSACTION + 4, data, reply, 0)
            reply.readException()
            reply.readInt()
        } finally {
            reply.recycle()
            data.recycle()
        }
    }
}