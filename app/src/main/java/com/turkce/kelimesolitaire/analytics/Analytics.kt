package com.turkce.kelimesolitaire.analytics

import android.app.Application

interface AnalyticsTracker {
    fun logEvent(name: String, params: Map<String, Any>)
}

object NoOpTracker : AnalyticsTracker {
    override fun logEvent(name: String, params: Map<String, Any>) {}
}

object Analytics {
    @Volatile
    private var tracker: AnalyticsTracker = NoOpTracker

    fun init(app: Application) {
        tracker = try {
            PlatformAnalytics.create(app)
        } catch (e: Throwable) {
            NoOpTracker   // آنالیتیکس هرگز نباید بازی را کرش کند
        }
    }

    fun log(name: String, params: Map<String, Any> = emptyMap()) {
        try { tracker.logEvent(name, params) } catch (_: Throwable) {}
    }
}