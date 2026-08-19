package com.aiva.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.aiva.core.util.SecureStorage

class GameControlService : LifecycleService() {
    
    companion object {
        const val CHANNEL_ID = "aiva_game_control"
        const val NOTIFICATION_ID = 1003
        const val EXTRA_MEDIA_PROJECTION = "media_projection"
        const val EXTRA_GAME_PROFILE = "game_profile"
    }
    
    private var mediaProjection: MediaProjection? = null
    private var gameProfile: String? = null
    private var isRunning = false
    
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
            gameProfile = it.getStringExtra(EXTRA_GAME_PROFILE)
        }
        
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        
        isRunning = true
        startGameLoop()
        
        return START_STICKY
    }
    
    private fun startGameLoop() {
        // Real-time game control loop
        // - Capture screen via MediaProjection
        // - Process frame with vision
        // - Execute touch commands
        // Target: <50ms loop time
    }
    
    override fun onDestroy() {
        isRunning = false
        mediaProjection?.stop()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AIVA Game Control",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Real-time game automation"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("AIVA Game Control")
            .setContentText("Controlling: ${gameProfile ?: "game"}")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    fun stopGameControl() {
        isRunning = false
        stopSelf()
    }
}