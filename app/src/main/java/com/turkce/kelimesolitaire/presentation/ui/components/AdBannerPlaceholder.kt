package com.turkce.kelimesolitaire.presentation.ui.components

import android.app.Activity
import android.util.Log
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType
import ir.tapsell.plus.model.TapsellPlusAdModel

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
                modifier = Modifier.fillMaxWidth(),
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
                modifier = Modifier.fillMaxWidth(),
                factory = { ctx ->
                    FrameLayout(ctx).apply {
                        val activity = ctx as? Activity
                        if (activity != null) {
                            try {
                                TapsellPlus.requestStandardBannerAd(
                                    activity,
                                    AdManager.TAPSELL_BANNER_ZONE_ID,
                                    TapsellPlusBannerType.BANNER_320x50,
                                    object : AdRequestCallback() {
                                        override fun response(model: TapsellPlusAdModel) {
                                            val respId = model.responseId ?: return
                                            TapsellPlus.showStandardBannerAd(
                                                activity,
                                                respId,
                                                this@apply,
                                                object : AdShowListener() {}
                                            )
                                        }

                                        override fun error(message: String?) {
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
