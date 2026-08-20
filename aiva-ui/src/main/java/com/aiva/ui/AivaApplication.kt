package com.aiva.ui

import android.app.Application

class AivaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize secure storage
        com.aiva.core.util.SecureStorage.init(this)
        // Generate encryption key
        com.aiva.core.util.KeyStoreManager.generateKey()
    }
}