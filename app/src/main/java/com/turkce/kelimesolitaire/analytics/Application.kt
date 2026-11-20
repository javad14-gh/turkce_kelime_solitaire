package com.turkce.kelimesolitaire

import android.app.Application
import com.turkce.kelimesolitaire.analytics.Analytics

class KelimeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Analytics.init(this)
    }
}