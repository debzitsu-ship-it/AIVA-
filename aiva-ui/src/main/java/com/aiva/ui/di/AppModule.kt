package com.aiva.ui.di

import android.content.Context
import com.aiva.ai.client.NimApiService
import com.aiva.ai.client.NimClient
import com.aiva.ai.fusion.MultiModelFusion
import com.aiva.ai.registry.ModelRegistry
import com.aiva.ai.router.ModelRouter
import com.aiva.automation.accessibility.AccessibilityController
import com.aiva.automation.accessibility.AivaAccessibilityService
import com.aiva.automation.executor.ActionExecutor
import com.aiva.core.util.SecureStorage
import com.aiva.game.engine.GameEngine
import com.aiva.game.vision.GameVisionProcessor
import com.aiva.memory.db.AivaDatabase
import com.aiva.memory.repository.ApiUsageRepository
import com.aiva.memory.repository.ConversationRepository
import com.aiva.memory.repository.GameProfileRepository
import com.aiva.memory.repository.TaskHistoryRepository
import com.aiva.memory.repository.UserPreferencesRepository
import com.aiva.security.ApiKeyManager
import com.aiva.security.PermissionManager
import com.aiva.task.classifier.IntentClassifier
import com.aiva.task.executor.TaskExecutor
import com.aiva.task.planner.TaskPlanner
import com.aiva.voice.riva.RivaAsrClient
import com.aiva.voice.riva.RivaTtsClient
import com.aiva.voice.service.VoiceInputService
import com.aiva.voice.service.VoiceOutputService
import com.aiva.voice.viewmodel.VoiceViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideSecureStorage(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): SecureStorage {
        SecureStorage.init(context)
        return SecureStorage
    }
    
    @Provides
    @Singleton
    fun provideDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): AivaDatabase {
        return AivaDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideConversationRepository(database: AivaDatabase): ConversationRepository {
        return ConversationRepository(database)
    }
    
    @Provides
    @Singleton
    fun provideTaskHistoryRepository(database: AivaDatabase): TaskHistoryRepository {
        return TaskHistoryRepository(database)
    }
    
    @Provides
    @Singleton
    fun provideUserPreferencesRepository(database: AivaDatabase): UserPreferencesRepository {
        return UserPreferencesRepository(database)
    }
    
    @Provides
    @Singleton
    fun provideGameProfileRepository(database: AivaDatabase): GameProfileRepository {
        return GameProfileRepository(database)
    }
    
    @Provides
    @Singleton
    fun provideApiUsageRepository(database: AivaDatabase): ApiUsageRepository {
        return ApiUsageRepository(database)
    }
    
    @Provides
    @Singleton
    fun provideApiKeyManager(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): ApiKeyManager {
        return ApiKeyManager(context)
    }
    
    @Provides
    @Singleton
    fun providePermissionManager(@dagger.hilt.android.qualifiers.ApplicationContext context: Context): PermissionManager {
        return PermissionManager(context)
    }
    
    @Provides
    @Singleton
    fun provideModelRegistry(): ModelRegistry {
        return ModelRegistry()
    }
    
    @Provides
    @Singleton
    fun provideModelRouter(modelRegistry: ModelRegistry): ModelRouter {
        return ModelRouter(modelRegistry)
    }
    
    @Provides
    @Singleton
    fun provideNimClient(): NimClient {
        return NimClient()
    }
    
    @Provides
    @Singleton
    fun provideMultiModelFusion(nimClient: NimClient, modelRegistry: ModelRegistry): MultiModelFusion {
        return MultiModelFusion(nimClient, modelRegistry)
    }
    
    // Voice
    @Provides
    @Singleton
    fun provideRivaAsrClient(): RivaAsrClient {
        return RivaAsrClient()
    }
    
    @Provides
    @Singleton
    fun provideRivaTtsClient(): RivaTtsClient {
        return RivaTtsClient()
    }
    
    @Provides
    @Singleton
    fun provideVoiceInputService(rivaAsrClient: RivaAsrClient): VoiceInputService {
        return VoiceInputService(rivaAsrClient)
    }
    
    @Provides
    @Singleton
    fun provideVoiceOutputService(rivaTtsClient: RivaTtsClient): VoiceOutputService {
        return VoiceOutputService(rivaTtsClient)
    }
    
    @Provides
    @Singleton
    fun provideVoiceViewModel(
        apiKeyManager: ApiKeyManager,
        voiceInputService: VoiceInputService,
        voiceOutputService: VoiceOutputService
    ): VoiceViewModel {
        return VoiceViewModel(apiKeyManager, voiceInputService, voiceOutputService)
    }
    
    // Automation - AccessibilityController provided via singleton instance
    @Provides
    @Singleton
    fun provideAccessibilityController(): AccessibilityController {
        val instance = AivaAccessibilityService.getInstance()
        return instance ?: object : AccessibilityController {
            override fun executeAction(action: com.aiva.core.action.Action): com.aiva.core.action.ActionResult {
                return com.aiva.core.action.ActionResult(success = false, message = "Accessibility service not enabled")
            }
            override val screenState: kotlinx.coroutines.flow.StateFlow<com.aiva.core.observation.ScreenState?> = kotlinx.coroutines.flow.MutableStateFlow(null).asStateFlow()
            override val serviceEnabled: kotlinx.coroutines.flow.StateFlow<Boolean> = kotlinx.coroutines.flow.MutableStateFlow(false).asStateFlow()
            override fun getCurrentScreenState(): com.aiva.core.observation.ScreenState? = null
            override fun cancelCurrentAction() {}
        }
    }
    
    @Provides
    @Singleton
    fun provideActionExecutor(accessibilityController: AccessibilityController): ActionExecutor {
        return ActionExecutor(accessibilityController)
    }
    
    // Task
    @Provides
    @Singleton
    fun provideIntentClassifier(
        nimClient: NimClient,
        modelRegistry: ModelRegistry,
        modelRouter: ModelRouter
    ): IntentClassifier {
        return IntentClassifier(nimClient, modelRegistry, modelRouter)
    }
    
    @Provides
    @Singleton
    fun provideTaskPlanner(
        nimClient: NimClient,
        modelRegistry: ModelRegistry,
        modelRouter: ModelRouter
    ): TaskPlanner {
        return TaskPlanner(nimClient, modelRegistry, modelRouter)
    }
    
    @Provides
    @Singleton
    fun provideTaskExecutor(
        intentClassifier: IntentClassifier,
        taskPlanner: TaskPlanner,
        actionExecutor: ActionExecutor
    ): TaskExecutor {
        return TaskExecutor(intentClassifier, taskPlanner, actionExecutor)
    }
    
    // Game
    @Provides
    @Singleton
    fun provideGameVisionProcessor(): GameVisionProcessor {
        return GameVisionProcessor()
    }
    
    @Provides
    @Singleton
    fun provideGameEngine(visionProcessor: GameVisionProcessor): GameEngine {
        return GameEngine(visionProcessor)
    }
}