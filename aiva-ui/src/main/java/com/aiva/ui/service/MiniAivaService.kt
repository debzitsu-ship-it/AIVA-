package com.aiva.ui.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.aiva.conversation.ConversationViewModel
import com.aiva.core.task.TaskState
import com.aiva.ui.mini.MiniAivaScreen
import com.aiva.ui.theme.AivaTheme
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MiniAivaService : Service(), ViewModelStoreOwner {
    
    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var isShowing = false
    private var initialX = 0f
    private var initialY = 0f
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    
    private val viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore
        get() = viewModelStore
    
    private val lifecycleRegistry = LifecycleRegistry(this)
    override fun getLifecycle() = lifecycleRegistry
    
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    
    private val viewModel: ConversationViewModel by lazy {
        ViewModelProvider(this, viewModelFactory).get(ConversationViewModel::class.java)
    }
    
    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_CREATE)
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        composeView = ComposeView(this).apply {
            setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                AivaTheme {
                    MiniAivaScreen(
                        viewModel = viewModel,
                        onDismiss = { hide() },
                        onExpand = { expandToFull() }
                    )
                }
            }
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        show()
        return START_STICKY
    }
    
    override fun onDestroy() {
        hide()
        lifecycleRegistry.handleLifecycleEvent(androidx.lifecycle.Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    fun show() {
        if (isShowing || composeView == null) return
        
        val params = WindowManager.LayoutParams(
            width = WindowManager.LayoutParams.WRAP_CONTENT,
            height = WindowManager.LayoutParams.WRAP_CONTENT,
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            format = PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }
        
        composeView?.setOnTouchListener { _, event ->
            handleTouch(event)
            true
        }
        
        try {
            windowManager?.addView(composeView!!, params)
            isShowing = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun hide() {
        if (!isShowing || composeView == null) return
        
        try {
            windowManager?.removeView(composeView!!)
            isShowing = false
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun handleTouch(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params().x.toFloat()
                initialY = params().y.toFloat()
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val params = params()
                params.x = (initialX + event.rawX - initialTouchX).toInt()
                params.y = (initialY + event.rawY - initialTouchY).toInt()
                windowManager?.updateViewLayout(composeView!!, params)
                return true
            }
            MotionEvent.ACTION_UP -> {
                // Check if near dismissal zone (bottom center)
                val screenWidth = resources.displayMetrics.widthPixels
                val screenHeight = resources.displayMetrics.heightPixels
                val dismissZone = android.graphics.Rect(
                    screenWidth / 2 - 100,
                    screenHeight - 200,
                    screenWidth / 2 + 100,
                    screenHeight
                )
                
                if (dismissZone.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    hide()
                    stopSelf()
                }
                return true
            }
        }
        return false
    }
    
    private fun params(): WindowManager.LayoutParams {
        return composeView?.layoutParams as? WindowManager.LayoutParams
            ?: WindowManager.LayoutParams()
    }
    
    private fun expandToFull() {
        hide()
        val intent = Intent(this, com.aiva.ui.MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}