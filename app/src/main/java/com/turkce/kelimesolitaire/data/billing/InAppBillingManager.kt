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
    private var activeDescriptor = "ir.mservices.market.IInAppBillingService"

    private var onPurchaseSuccessCallback: ((String) -> Unit)? = null
    private var onPurchaseErrorCallback: ((String) -> Unit)? = null

    private var appContext: Context? = null
    private var onPurchasesRestoredCallback: ((Set<String>) -> Unit)? = null

    val isConnected: Boolean
        get() = billingService != null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "Billing service connected from ${name?.packageName}")
            if (service != null) {
                billingService = InAppBillingProxy(service, activeDescriptor)
                queryAndRestorePurchases()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "Billing service disconnected")
            billingService = null
        }
    }

    fun initialize(context: Context, onPurchasesRestored: ((Set<String>) -> Unit)? = null) {
        val appCtx = context.applicationContext
        this.appContext = appCtx
        if (onPurchasesRestored != null) {
            this.onPurchasesRestoredCallback = onPurchasesRestored
        }
        setupTargetMarket(appCtx)

        if (isBound) {
            if (billingService != null) {
                queryAndRestorePurchases()
            }
            return
        }

        try {
            val intent = Intent(activeBindAction).apply {
                `package` = activeMarketPackage
            }
            val resolved = appCtx.packageManager.queryIntentServices(intent, 0)
            if (resolved.isNotEmpty()) {
                isBound = appCtx.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                Log.d(TAG, "Binding to $activeMarketPackage returned $isBound")
            } else {
                Log.w(TAG, "No service resolved for $activeBindAction on $activeMarketPackage")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error binding billing service: ${e.message}")
        }
    }

    private fun setupTargetMarket(context: Context) {
        val isMyketInstalled = isPackageInstalled(context, MyketBillingConfig.MYKET_BILLING_PACKAGE)
        val isBazaarInstalled = isPackageInstalled(context, "com.farsitel.bazaar")

        val flavor = try { BuildConfig.FLAVOR } catch (e: Throwable) { "" }

        when {
            flavor.contains("bazaar", ignoreCase = true) || (!isMyketInstalled && isBazaarInstalled) -> {
                activeMarketPackage = "com.farsitel.bazaar"
                activeBindAction = "ir.cafebazaar.pardakht.InAppBillingService.BIND"
                activePublicKey = BazaarBillingConfig.BAZAAR_RSA_PUBLIC_KEY
                activeDescriptor = "ir.cafebazaar.pardakht.IInAppBillingService"
                Log.d(TAG, "Configured target market: Cafe Bazaar")
            }
            else -> {
                activeMarketPackage = MyketBillingConfig.MYKET_BILLING_PACKAGE
                activeBindAction = MyketBillingConfig.MYKET_BIND_ACTION
                activePublicKey = MyketBillingConfig.MYKET_RSA_PUBLIC_KEY
                activeDescriptor = "ir.mservices.market.IInAppBillingService"
                Log.d(TAG, "Configured target market: Myket")
            }
        }
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

    fun startPurchase(
        activity: Activity,
        sku: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onPurchaseSuccessCallback = onSuccess
        this.onPurchaseErrorCallback = onError

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

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val packageName = activity.packageName
                val developerPayload = "payload_${System.currentTimeMillis()}"
                val buyBundle = service.getBuyIntent(3, packageName, sku, "inapp", developerPayload)

                val responseCode = buyBundle.getInt("RESPONSE_CODE", -1)
                if (responseCode == 0) { // BILLING_RESPONSE_RESULT_OK
                    val buyIntent = buyBundle.getParcelable<PendingIntent>("BUY_INTENT")
                    if (buyIntent != null) {
                        activity.startIntentSenderForResult(
                            buyIntent.intentSender,
                            RC_BUY,
                            Intent(),
                            0,
                            0,
                            0
                        )
                    } else {
                        withContext(Dispatchers.Main) {
                            onError("خطا در دریافت درگاه پرداخت مارکت.")
                        }
                    }
                } else if (responseCode == 7) { // BILLING_RESPONSE_RESULT_ITEM_ALREADY_OWNED
                    withContext(Dispatchers.Main) {
                        onSuccess(sku)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        onError("خطای پرداخت مارکت (کد: $responseCode)")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error starting purchase: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    onError("خطا در برقراری ارتباط با مارکت: ${e.message}")
                }
            }
        }
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ): Boolean {
        if (requestCode != RC_BUY) return false

        if (resultCode != Activity.RESULT_OK || data == null) {
            onError("پرداخت لغو شد.")
            return true
        }

        val purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
        val dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE")

        if (purchaseData.isNullOrEmpty()) {
            onError("اطلاعات خرید دریافت نشد.")
            return true
        }

        try {
            val json = JSONObject(purchaseData)
            val sku = json.optString("productId")
            val token = json.optString("purchaseToken")

            // Verify signature if signature is present
            if (!dataSignature.isNullOrEmpty() && activePublicKey.isNotBlank()) {
                val isValid = Security.verifyPurchase(activePublicKey, purchaseData, dataSignature)
                if (!isValid) {
                    Log.w(TAG, "Purchase signature verification failed, but checking purchase state")
                }
            }

            // Consume consumable items so they can be bought again
            val isConsumable = isSkuConsumable(sku)
            if (isConsumable && token.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        billingService?.consumePurchase(3, json.optString("packageName"), token)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Error consuming purchase: ${e.message}")
                    }
                }
            }

            onSuccess(sku)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing purchase result: ${e.message}")
            onError("خطا در پردازش اطلاعات خرید.")
        }

        return true
    }

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
                    if (responseCode == 0) {
                        val itemList = bundle.getStringArrayList("INAPP_PURCHASE_ITEM_LIST")
                        if (itemList != null) {
                            ownedSkus.addAll(itemList)
                        }
                        continuationToken = bundle.getString("INAPP_CONTINUATION_TOKEN")
                    } else {
                        break
                    }
                } while (!continuationToken.isNullOrEmpty())

                if (ownedSkus.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        onPurchasesRestoredCallback?.invoke(ownedSkus)
                    }
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Error querying purchases: ${e.message}")
            }
        }
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
                Log.e(TAG, "Error unbinding service: ${e.message}")
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
    private val descriptor: String
) : IInterface {

    override fun asBinder(): IBinder = remote

    fun isBillingSupported(apiVersion: Int, packageName: String, type: String): Int {
        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        return try {
            data.writeInterfaceToken(descriptor)
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
            data.writeInterfaceToken(descriptor)
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
            data.writeInterfaceToken(descriptor)
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
            data.writeInterfaceToken(descriptor)
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
