package com.aiva.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AivaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize secure storage
        com.aiva.core.util.SecureStorage.init(this)
        // Generate encryption key
        com.aiva.core.util.KeyStoreManager.generateKey()
    }
}