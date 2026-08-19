package com.aiva.ui

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.os.ParcelFileDescriptor
import android.util.Log
import com.aiva.core.util.SecureStorage
import java.io.IOException

/**
 * Custom backup agent that excludes sensitive data from backup
 * API keys, encryption keys, and screen captures are never backed up
 */
class SecureBackupAgent : BackupAgent() {
    
    private val TAG = "SecureBackupAgent"
    
    override fun onCreate() {
        super.onCreate()
        SecureStorage.init(this)
    }
    
    override fun onBackup(oldState: ParcelFileDescriptor?, data: BackupDataOutput?, newState: ParcelFileDescriptor?) {
        // Only backup non-sensitive preferences
        // SecureStorage (API keys) is explicitly excluded via backup_rules.xml
        // Room database is excluded via backup_rules.xml
        
        Log.d(TAG, "Backup requested - only non-sensitive data will be backed up")
        
        // We could write safe preferences here if needed
        // For now, we rely on the XML rules to exclude sensitive data
        super.onBackup(oldState, data, newState)
    }
    
    override fun onRestore(data: BackupDataInput?, appVersionCode: Int, newState: ParcelFileDescriptor?) {
        Log.d(TAG, "Restore requested")
        
        // Restore non-sensitive data
        // SecureStorage will be re-initialized on app start
        super.onRestore(data, appVersionCode, newState)
    }
    
    override fun onFullBackup(data: BackupDataOutput?) {
        // Full backup (Android 6.0+) - exclude sensitive data
        // We use the XML rules to control what gets backed up
        super.onFullBackup(data)
    }
}