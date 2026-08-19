package com.aiva.ui.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.aiva.core.util.SecureStorage

class VoiceCaptureService : LifecycleService() {
    
    companion object {
        const val CHANNEL_ID = "aiva_voice_capture"
        const val NOTIFICATION_ID = 1002
    }
    
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        SecureStorage.init(this)
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildNotification()
        startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        return START_STICKY
    }
    
    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    fun startRecording() {
        if (isRecording) return
        
        val sampleRate = 16000
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            .coerceAtLeast(4096)
        
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelConfig,
            audioFormat,
            bufferSize
        ).also { record ->
            record.startRecording()
            isRecording = true
            
            // Process audio in background
            // In real implementation, send to Riva ASR
        }
    }
    
    fun stopRecording() {
        if (!isRecording) return
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AIVA Voice Capture",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Captures voice input for AIVA"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("AIVA Voice")
            .setContentText("Listening for voice commands")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
}