package com.turkce.kelimesolitaire.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState

class AdManager private constructor() {

    companion object {
        private const val TAG = "AdManager"

        // Official Google Test Ad Unit IDs (For Google Play / Turkish build)
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

        // Tapsell IDs (For Cafe Bazaar / Persian build)
        const val TAPSELL_APP_KEY = "jqfpqlgflojgicbkqnidhnmppgppjeoqedirholtnaffhkclhoaffepsaphecmrtmhtjnl"
        const val TAPSELL_BANNER_ZONE_ID = "6ab2ee0ee237e15c69fba7fd"
        const val TAPSELL_INTERSTITIAL_ZONE_ID = "6ab2ede6e237e15c69fba7fc"
        const val TAPSELL_REWARDED_ZONE_ID = "6ab2edf6f9c3d5797ba49a96"

        @Volatile
        private var instance: AdManager? = null

        fun getInstance(): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager().also { instance = it }
            }
        }
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    // Tapsell ad response IDs
    private var tapsellInterstitialResponseId: String? = null
    private var tapsellRewardedResponseId: String? = null
    private var isTapsellInitialized = false

    fun isTapsellConfigured(): Boolean {
        return TAPSELL_APP_KEY.isNotBlank() && TAPSELL_APP_KEY != "YOUR_TAPSELL_APP_KEY"
    }

    fun initialize(context: Context, onComplete: () -> Unit = {}) {
        val isPersian = LocaleHelper.isPersian(context)
        Log.d(TAG, "Initializing AdManager. isPersian=$isPersian")

        if (isPersian) {
            // Initialize Tapsell for Iranian market
            if (isTapsellConfigured()) {
                try {
                    Tapsell.setInitializationListener {
                        Log.d(TAG, "Tapsell Mediation initialized successfully.")
                        isTapsellInitialized = true
                        loadInterstitial(context)
                        loadRewarded(context)
                        onComplete()
                    }
                    isTapsellInitialized = true
                    loadInterstitial(context)
                    loadRewarded(context)
                    onComplete()
                } catch (e: Throwable) {
                    Log.e(TAG, "Failed to initialize Tapsell: ${e.message}")
                    onComplete()
                }
            } else {
                Log.i(TAG, "Tapsell is in placeholder mode. Real ads disabled until key is added.")
                onComplete()
            }
        } else {
            // Initialize Google AdMob for Global / Turkish market
            try {
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "MobileAds initialized: ${status.adapterStatusMap}")
                    loadInterstitial(context)
                    loadRewarded(context)
                    onComplete()
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to initialize MobileAds: ${e.message}")
                onComplete()
            }
        }
    }

    // --- INTERSTITIAL ADS ---
    
    fun loadInterstitial(context: Context, adUnitId: String = TEST_INTERSTITIAL_ID) {
        if (LocaleHelper.isPersian(context)) {
            if (isTapsellConfigured()) {
                try {
                    val activity = generateSequence(context) { if (it is android.content.ContextWrapper) it.baseContext else null }
                        .filterIsInstance<Activity>()
                        .firstOrNull()
                    val listener = object : RequestResultListener {
                        override fun onSuccess(adId: String) {
                            tapsellInterstitialResponseId = adId
                            Log.d(TAG, "Tapsell Interstitial loaded: $adId")
                        }

                        override fun onFailure(message: String) {
                            Log.e(TAG, "Tapsell Interstitial load error: $message")
                            tapsellInterstitialResponseId = null
                        }
                    }
                    if (activity != null) {
                        Tapsell.requestInterstitialAd(TAPSELL_INTERSTITIAL_ZONE_ID, activity, listener)
                    } else {
                        Tapsell.requestInterstitialAd(TAPSELL_INTERSTITIAL_ZONE_ID, listener)
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "Tapsell requestInterstitialAd exception: ${e.message}")
                }
            }
            return
        }

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Interstitial ad failed to load: ${adError.message}")
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial ad loaded successfully.")
                    interstitialAd = ad
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onDismissed: () -> Unit) {
        if (LocaleHelper.isPersian(activity)) {
            val respId = tapsellInterstitialResponseId
            if (isTapsellConfigured() && isTapsellInitialized && !respId.isNullOrEmpty()) {
                try {
                    Tapsell.showInterstitialAd(
                        respId,
                        activity,
                        object : AdStateListener.Interstitial {
                            override fun onAdClosed(completionState: AdShowCompletionState) {
                                Log.d(TAG, "Tapsell interstitial closed: ${completionState.name}")
                                tapsellInterstitialResponseId = null
                                loadInterstitial(activity)
                                onDismissed()
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "Tapsell interstitial impression")
                            }

                            override fun onAdClicked() {
                                Log.d(TAG, "Tapsell interstitial clicked")
                            }

                            override fun onAdFailed(message: String) {
                                Log.e(TAG, "Tapsell interstitial show error: $message")
                                tapsellInterstitialResponseId = null
                                loadInterstitial(activity)
                                onDismissed()
                            }
                        }
                    )
                } catch (e: Throwable) {
                    Log.e(TAG, "Tapsell showInterstitial error: ${e.message}")
                    onDismissed()
                }
            } else {
                loadInterstitial(activity)
                onDismissed()
            }
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed.")
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                    interstitialAd = null
                    onDismissed()
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad not ready yet. Skipping to logic.")
            loadInterstitial(activity)
            onDismissed()
        }
    }

    // --- REWARDED ADS ---

    fun loadRewarded(context: Context, adUnitId: String = TEST_REWARDED_ID) {
        if (LocaleHelper.isPersian(context)) {
            if (isTapsellConfigured()) {
                try {
                    val activity = generateSequence(context) { if (it is android.content.ContextWrapper) it.baseContext else null }
                        .filterIsInstance<Activity>()
                        .firstOrNull()
                    val listener = object : RequestResultListener {
                        override fun onSuccess(adId: String) {
                            tapsellRewardedResponseId = adId
                            Log.d(TAG, "Tapsell Rewarded ad loaded: $adId")
                        }

                        override fun onFailure(message: String) {
                            Log.e(TAG, "Tapsell Rewarded ad load error: $message")
                            tapsellRewardedResponseId = null
                        }
                    }
                    if (activity != null) {
                        Tapsell.requestRewardedAd(TAPSELL_REWARDED_ZONE_ID, activity, listener)
                    } else {
                        Tapsell.requestRewardedAd(TAPSELL_REWARDED_ZONE_ID, listener)
                    }
                } catch (e: Throwable) {
                    Log.e(TAG, "Tapsell requestRewardedAd exception: ${e.message}")
                }
            }
            return
        }

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Rewarded ad loaded successfully.")
                    rewardedAd = ad
                }
            }
        )
    }

    fun showRewarded(activity: Activity, onRewardEarned: (amount: Int) -> Unit) {
        if (LocaleHelper.isPersian(activity)) {
            val respId = tapsellRewardedResponseId
            if (isTapsellConfigured() && !respId.isNullOrEmpty()) {
                showTapsellRewardedAdInternal(respId, activity, onRewardEarned)
            } else if (isTapsellConfigured()) {
                android.widget.Toast.makeText(activity, "در حال دریافت ویدیو تبلیغاتی...", android.widget.Toast.LENGTH_SHORT).show()
                try {
                    Tapsell.requestRewardedAd(
                        TAPSELL_REWARDED_ZONE_ID,
                        activity,
                        object : RequestResultListener {
                            override fun onSuccess(adId: String) {
                                Log.d(TAG, "Tapsell Rewarded ad loaded on demand: $adId")
                                tapsellRewardedResponseId = adId
                                showTapsellRewardedAdInternal(adId, activity, onRewardEarned)
                            }

                            override fun onFailure(message: String) {
                                Log.e(TAG, "Tapsell Rewarded ad load on demand error: $message")
                                android.widget.Toast.makeText(
                                    activity,
                                    "ویدیویی برای نمایش موجود نیست. لطفاً مجدداً امتحان کنید.",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    )
                } catch (e: Throwable) {
                    Log.e(TAG, "Tapsell requestRewardedAd exception: ${e.message}")
                    android.widget.Toast.makeText(activity, "خطا در اتصال به سرویس تبلیغات", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                android.widget.Toast.makeText(activity, "سرویس تبلیغات پیکربندی نشده است.", android.widget.Toast.LENGTH_SHORT).show()
            }
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed.")
                    rewardedAd = null
                    loadRewarded(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                }
            }
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onRewardEarned(rewardItem.amount)
            }
        } else {
            Log.d(TAG, "Rewarded ad not ready yet. Simulating reward for fallback.")
            onRewardEarned(50)
            loadRewarded(activity)
        }
    }

    private fun showTapsellRewardedAdInternal(
        adId: String,
        activity: Activity,
        onRewardEarned: (amount: Int) -> Unit
    ) {
        try {
            var rewarded = false
            Tapsell.showRewardedAd(
                adId,
                activity,
                object : AdStateListener.Rewarded {
                    override fun onRewarded() {
                        Log.d(TAG, "Tapsell onRewarded")
                        rewarded = true
                        onRewardEarned(50)
                    }

                    override fun onAdClosed(completionState: AdShowCompletionState) {
                        Log.d(TAG, "Tapsell rewarded closed: ${completionState.name}")
                        tapsellRewardedResponseId = null
                        loadRewarded(activity)
                        if (!rewarded && completionState == AdShowCompletionState.COMPLETED) {
                            onRewardEarned(50)
                        }
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "Tapsell rewarded impression")
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "Tapsell rewarded clicked")
                    }

                    override fun onAdFailed(message: String) {
                        Log.e(TAG, "Tapsell rewarded show error: $message")
                        tapsellRewardedResponseId = null
                        loadRewarded(activity)
                        android.widget.Toast.makeText(activity, "خطا در پخش ویدیو: $message", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } catch (e: Throwable) {
            Log.e(TAG, "Tapsell showRewardedAd error: ${e.message}")
        }
    }
}
