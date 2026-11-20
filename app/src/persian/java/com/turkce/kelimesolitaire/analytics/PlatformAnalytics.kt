package com.turkce.kelimesolitaire.analytics

import android.app.Application
import com.turkce.kelimesolitaire.BuildConfig
import io.appmetrica.analytics.AppMetrica
import io.appmetrica.analytics.AppMetricaConfig

object PlatformAnalytics {
    private const val API_KEY = "6321a1a1-3c17-4754-b58f-884debe4e200"

    fun create(app: Application): AnalyticsTracker {
        val config = AppMetricaConfig.newConfigBuilder(API_KEY)
            .apply { if (BuildConfig.DEBUG) withLogs() }
            .build()
        AppMetrica.activate(app, config)
        AppMetrica.enableActivityAutoTracking(app)

        return object : AnalyticsTracker {
            override fun logEvent(name: String, params: Map<String, Any>) {
                AppMetrica.reportEvent(name, params)
            }
        }
    }
}