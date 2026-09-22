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
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworkError
import ir.tapsell.plus.model.AdNetworks
import ir.tapsell.plus.model.TapsellPlusAdModel
import ir.tapsell.plus.model.TapsellPlusErrorModel

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
                    TapsellPlus.initialize(context, TAPSELL_APP_KEY, object : TapsellPlusInitListener {
                        override fun onInitializeSuccess(adNetworks: AdNetworks?) {
                            Log.d(TAG, "TapsellPlus connected successfully.")
                            isTapsellInitialized = true
                            // Preload initial ads
                            loadInterstitial(context)
                            loadRewarded(context)
                            onComplete()
                        }

                        override fun onInitializeFailed(adNetworks: AdNetworks?, adNetworkError: AdNetworkError?) {
                            Log.e(TAG, "TapsellPlus initialization error: ${adNetworkError?.errorMessage}")
                            isTapsellInitialized = false
                            onComplete()
                        }
                    })
                } catch (e: Throwable) {
                    Log.e(TAG, "Failed to initialize TapsellPlus: ${e.message}")
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
            if (context is Activity && isTapsellConfigured() && isTapsellInitialized) {
                try {
                    TapsellPlus.requestInterstitialAd(context, TAPSELL_INTERSTITIAL_ZONE_ID, object : AdRequestCallback() {
                        override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                            tapsellInterstitialResponseId = tapsellPlusAdModel.responseId
                            Log.d(TAG, "Tapsell Interstitial loaded.")
                        }

                        override fun error(message: String?) {
                            Log.e(TAG, "Tapsell Interstitial load error: $message")
                            tapsellInterstitialResponseId = null
                        }
                    })
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
            if (isTapsellConfigured() && isTapsellInitialized && respId != null) {
                try {
                    TapsellPlus.showInterstitialAd(activity, respId, object : AdShowListener() {
                        override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                            tapsellInterstitialResponseId = null
                            loadInterstitial(activity)
                            onDismissed()
                        }

                        override fun onError(error: TapsellPlusErrorModel) {
                            Log.e(TAG, "Tapsell interstitial show error: ${error.errorMessage}")
                            tapsellInterstitialResponseId = null
                            loadInterstitial(activity)
                            onDismissed()
                        }
                    })
                } catch (e: Throwable) {
                    Log.e(TAG, "Tapsell showInterstitial error: ${e.message}")
                    onDismissed()
                }
            } else {
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
            if (context is Activity && isTapsellConfigured() && isTapsellInitialized) {
                try {
                    TapsellPlus.requestRewardedVideoAd(context, TAPSELL_REWARDED_ZONE_ID, object : AdRequestCallback() {
                        override fun response(tapsellPlusAdModel: TapsellPlusAdModel) {
                            tapsellRewardedResponseId = tapsellPlusAdModel.responseId
                            Log.d(TAG, "Tapsell Rewarded ad loaded.")
                        }

                        override fun error(message: String?) {
                            Log.e(TAG, "Tapsell Rewarded ad load error: $message")
                            tapsellRewardedResponseId = null
                        }
                    })
                } catch (e: Throwable) {
                    Log.e(TAG, "Tapsell requestRewardedVideoAd exception: ${e.message}")
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
            if (isTapsellConfigured() && isTapsellInitialized && respId != null) {
                try {
                    TapsellPlus.showRewardedVideoAd(activity, respId, object : AdShowListener() {
                        override fun onRewarded(tapsellPlusAdModel: TapsellPlusAdModel) {
                            tapsellRewardedResponseId = null
                            loadRewarded(activity)
                            onRewardEarned(50)
                        }

                        override fun onError(error: TapsellPlusErrorModel) {
                            Log.e(TAG, "Tapsell rewarded show error: ${error.errorMessage}")
                            tapsellRewardedResponseId = null
                            loadRewarded(activity)
                            onRewardEarned(50) // Safe simulation fallback
                        }

                        override fun onClosed(tapsellPlusAdModel: TapsellPlusAdModel) {
                            tapsellRewardedResponseId = null
                            loadRewarded(activity)
                        }
                    })
                } catch (e: Throwable) {
                    Log.e(TAG, "Tapsell showRewardedVideoAd error: ${e.message}")
                    onRewardEarned(50)
                }
            } else {
                // Safe simulation when in placeholder/test mode
                onRewardEarned(50)
                if (isTapsellConfigured()) {
                    loadRewarded(activity)
                }
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
}
