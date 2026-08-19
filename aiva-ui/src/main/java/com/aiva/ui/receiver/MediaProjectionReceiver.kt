package com.aiva.ui.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.util.Log

class MediaProjectionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_MEDIA_PROJECTION_STOPPED = "android.media.projection.ACTION_MEDIA_PROJECTION_STOPPED"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ACTION_MEDIA_PROJECTION_STOPPED) {
            Log.d("MediaProjectionReceiver", "Media projection stopped")
            // Notify services that screen capture is no longer available
            // In real implementation, would use EventBus or similar
        }
    }
}