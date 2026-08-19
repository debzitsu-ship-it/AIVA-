package com.aiva.ui

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.os.ParcelFileDescriptor
import com.aiva.core.util.SecureStorage

class SecureBackupAgent : BackupAgent() {

    override fun onCreate() {
        super.onCreate()
        SecureStorage.init(this)
    }

    override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput?,
        newState: ParcelFileDescriptor?
    ) {
        // Sensitive data is excluded via backup_rules.xml
    }

    override fun onRestore(
        data: BackupDataInput?,
        appVersionCode: Int,
        newState: ParcelFileDescriptor?
    ) {
        // No-op restore
    }
}
