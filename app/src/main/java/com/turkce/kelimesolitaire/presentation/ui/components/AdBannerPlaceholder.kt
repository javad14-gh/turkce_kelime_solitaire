package com.turkce.kelimesolitaire.presentation.ui.components

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.turkce.kelimesolitaire.ads.AdManager
import com.turkce.kelimesolitaire.presentation.ui.theme.BorderGlass
import com.turkce.kelimesolitaire.presentation.ui.theme.DarkCard
import com.turkce.kelimesolitaire.presentation.util.LocaleHelper
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.views.banner.BannerContainer

@Composable
fun AdBannerPlaceholder(
    modifier: Modifier = Modifier,
    isAdFree: Boolean = false,
    adUnitId: String = AdManager.TEST_BANNER_ID
) {
    val context = LocalContext.current
    val isPersian = remember(context) { LocaleHelper.isPersian(context) }
    val isTapsellConfigured = remember { AdManager.getInstance().isTapsellConfigured() }

    // If ad-free or in Persian build without active Tapsell keys yet, hide gracefully
    if (isAdFree || (isPersian && !isTapsellConfigured)) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkCard)
            .border(1.dp, BorderGlass, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!isPersian) {
            // Google AdMob Banner (Google Play build)
            AndroidView(
                modifier = Modifier.wrapContentSize(Alignment.Center),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        setAdUnitId(adUnitId)
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { adView ->
                    adView.destroy()
                }
            )
        } else {
            // Tapsell Banner (Bazaar build)
            AndroidView(
                modifier = Modifier.wrapContentSize(Alignment.Center),
                factory = { ctx ->
                    BannerContainer(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = android.view.Gravity.CENTER
                        }
                        val activity = ctx as? Activity
                        if (activity != null) {
                            try {
                                Tapsell.requestBannerAd(
                                    AdManager.TAPSELL_BANNER_ZONE_ID,
                                    BannerSize.BANNER_320_50,
                                    object : RequestResultListener {
                                        override fun onSuccess(adId: String) {
                                            Tapsell.showBannerAd(
                                                adId,
                                                this@apply,
                                                activity,
                                                object : AdStateListener.Banner {
                                                    override fun onAdClicked() {
                                                        Log.d("AdBanner", "Tapsell banner onAdClicked")
                                                    }

                                                    override fun onAdFailed(message: String) {
                                                        Log.e("AdBanner", "Tapsell banner onAdFailed: $message")
                                                    }

                                                    override fun onAdImpression() {
                                                        Log.d("AdBanner", "Tapsell banner onAdImpression")
                                                    }
                                                }
                                            )
                                        }

                                        override fun onFailure(message: String) {
                                            Log.e("AdBanner", "Tapsell banner load error: $message")
                                        }
                                    }
                                )
                            } catch (e: Throwable) {
                                Log.e("AdBanner", "Tapsell banner exception: ${e.message}")
                            }
                        }
                    }
                }
            )
        }
    }
}
