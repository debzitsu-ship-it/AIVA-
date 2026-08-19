package com.aiva.ui.di

import android.content.Context
import com.aiva.automation.accessibility.AccessibilityController
import com.aiva.automation.accessibility.AivaAccessibilityService
import com.aiva.core.action.Action
import com.aiva.core.action.ActionResult
import com.aiva.core.observation.ScreenState
import com.aiva.core.util.SecureStorage
import com.aiva.memory.db.AivaDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSecureStorage(@ApplicationContext context: Context): SecureStorage {
        SecureStorage.init(context)
        return SecureStorage
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AivaDatabase {
        return AivaDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideAccessibilityController(): AccessibilityController {
        return AivaAccessibilityService.getInstance() ?: object : AccessibilityController {
            override fun executeAction(action: Action): ActionResult {
                return ActionResult(success = false, message = "Accessibility service not enabled")
            }

            override val screenState: StateFlow<ScreenState?> = MutableStateFlow(null)
            override val serviceEnabled: StateFlow<Boolean> = MutableStateFlow(false)
            override fun getCurrentScreenState(): ScreenState? = null
            override fun cancelCurrentAction() {}
        }
    }
}
