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

class AdManager private constructor() {

    companion object {
        private const val TAG = "AdManager"

        // Official Google Test Ad Unit IDs
        const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

        @Volatile
        private var instance: AdManager? = null

        fun getInstance(): AdManager =
            instance ?: synchronized(this) {
                instance ?: AdManager().also { instance = it }
            }
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var isInitialized = false

    fun initialize(context: Context, onComplete: () -> Unit = {}) {
        if (isInitialized) {
            onComplete()
            return
        }
        try {
            MobileAds.initialize(context) { status ->
                isInitialized = true
                Log.d(TAG, "AdMob SDK Initialized successfully. Status: $status")
                onComplete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob SDK initialization failed: ${e.message}")
            onComplete()
        }
    }

    // --- INTERSTITIAL ADS ---
    
    fun loadInterstitial(context: Context, adUnitId: String = TEST_INTERSTITIAL_ID) {
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
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial ad dismissed.")
                    interstitialAd = null
                    // Preload the next interstitial
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
            // Safe simulation in case ads aren't loaded in test environment
            onRewardEarned(50) // e.g. give 50 fallback coins
            loadRewarded(activity)
        }
    }
}
