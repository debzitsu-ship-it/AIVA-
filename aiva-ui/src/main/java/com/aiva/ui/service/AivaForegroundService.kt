package com.aiva.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.aiva.core.util.SecureStorage

class AivaForegroundService : LifecycleService() {
    
    companion object {
        const val CHANNEL_ID = "aiva_foreground"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_MEDIA_PROJECTION = "media_projection"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA = "data"
    }
    
    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionManager: MediaProjectionManager? = null
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        SecureStorage.init(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val projection = it.getParcelableExtra<MediaProjection>(EXTRA_MEDIA_PROJECTION)
            if (projection != null) {
                mediaProjection = projection
            }
        }
        
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        mediaProjection?.stop()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AIVA Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs AIVA automation and screen observation in background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_manage)
            .setContentTitle("AIVA")
            .setContentText("Running in background")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setShowWhen(false)
            .build()
    }
    
    fun getMediaProjection(): MediaProjection? = mediaProjection
    
    fun setMediaProjection(projection: MediaProjection) {
        mediaProjection = projection
    }
}