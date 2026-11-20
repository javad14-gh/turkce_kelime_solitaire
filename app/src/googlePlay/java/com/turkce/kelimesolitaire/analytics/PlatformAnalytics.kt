package com.turkce.kelimesolitaire.analytics

import android.app.Application

object PlatformAnalytics {
    fun create(app: Application): AnalyticsTracker = NoOpTracker
}