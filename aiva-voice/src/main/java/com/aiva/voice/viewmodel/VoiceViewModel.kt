package com.aiva.voice.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aiva.core.security.ApiKeyManager
import com.aiva.core.voice.VoiceConfig
import com.aiva.core.voice.VoiceState
import com.aiva.voice.service.VoiceInputService
import com.aiva.voice.service.VoiceOutputService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceViewModel @Inject constructor(
    private val apiKeyManager: ApiKeyManager,
    private val voiceInputService: VoiceInputService,
    private val voiceOutputService: VoiceOutputService
) : ViewModel() {
    
    private val _config = MutableStateFlow<VoiceConfig>(VoiceConfig())
    val config: StateFlow<VoiceConfig> = _config
    
    private val _isListening = MutableStateFlow<Boolean>(false)
    val isListening: StateFlow<Boolean> = _isListening
    
    private val _isSpeaking = MutableStateFlow<Boolean>(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking
    
    private val _partialText = MutableStateFlow<String>("")
    val partialText: StateFlow<String> = _partialText
    
    private val _recognizedText = MutableStateFlow<String>("")
    val recognizedText: StateFlow<String> = _recognizedText
    
    // Combined state
    val voiceState: StateFlow<VoiceState> = combine(
        voiceInputService.state,
        voiceOutputService.state
    ) { inputState, outputState ->
        when {
            inputState == VoiceState.LISTENING -> VoiceState.LISTENING
            outputState == VoiceState.SPEAKING -> VoiceState.SPEAKING
            inputState == VoiceState.ERROR || outputState == VoiceState.ERROR -> VoiceState.ERROR
            else -> VoiceState.IDLE
        }
    }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(), VoiceState.IDLE)
    
    init {
        loadConfig()
        observeServices()
    }
    
    private fun loadConfig() {
        viewModelScope.launch {
            val asrEnabled = apiKeyManager.getBoolean("voice_asr_enabled", true)
            val ttsEnabled = apiKeyManager.getBoolean("voice_tts_enabled", true)
            val autoSpeak = apiKeyManager.getBoolean("voice_auto_speak", false)
            val language = apiKeyManager.getString("voice_language", "en-US")
            val voice = apiKeyManager.getString("voice_voice", "Chatterbox-Multilingual.en-US.Male")
            val speed = apiKeyManager.getString("voice_speed", "1.0").toFloatOrNull() ?: 1.0f
            val volume = apiKeyManager.getString("voice_volume", "1.0").toFloatOrNull() ?: 1.0f
            val interruptOnUserSpeech = apiKeyManager.getBoolean("voice_interrupt", true)
            val vadSensitivity = apiKeyManager.getString("voice_vad", "0.5").toFloatOrNull() ?: 0.5f
            
            _config.value = VoiceConfig(
                asrEnabled = asrEnabled,
                ttsEnabled = ttsEnabled,
                autoSpeak = autoSpeak,
                language = language,
                voiceName = voice,
                speechSpeed = speed,
                volume = volume,
                interruptOnUserSpeech = interruptOnUserSpeech,
                vadSensitivity = vadSensitivity
            )
        }
    }
    
    private fun observeServices() {
        viewModelScope.launch {
            voiceInputService.partialResults.collect { text ->
                _partialText.value = text
            }
        }
        
        viewModelScope.launch {
            voiceInputService.finalResults.collect { text ->
                _recognizedText.value = text
                _isListening.value = false
            }
        }
        
        viewModelScope.launch {
            voiceOutputService.state.collect { state ->
                _isSpeaking.value = (state == VoiceState.SPEAKING)
            }
        }
    }
    
    fun startListening(onResult: (String) -> Unit) {
        if (!_config.value.asrEnabled) return
        
        viewModelScope.launch {
            val keys = apiKeyManager.getAllKeys()
            val asrKey = keys.firstOrNull { it.models.any { it.contains("asr") || it.contains("whisper") } }
                ?: keys.firstOrNull()
            
            asrKey?.let { key ->
                val decryptedKey = key.encryptedKey
                _isListening.value = true
                _partialText.value = ""
                _recognizedText.value = ""
                
                voiceInputService.startListening(
                    apiKey = com.aiva.core.util.KeyStoreManager.decryptString(decryptedKey),
                    language = _config.value.language,
                    onPartialResult = { _partialText.value = it },
                    onFinalResult = { text ->
                        _recognizedText.value = text
                        onResult(text)
                    }
                )
            }
        }
    }
    
    fun stopListening(): String {
        _isListening.value = false
        return voiceInputService.stopListening()
    }
    
    fun cancelListening() {
        _isListening.value = false
        _partialText.value = ""
        voiceInputService.cancelListening()
    }
    
    fun speak(text: String) {
        if (!_config.value.ttsEnabled || text.isBlank()) return
        
        viewModelScope.launch {
            val keys = apiKeyManager.getAllKeys()
            val ttsKey = keys.firstOrNull { it.models.any { it.contains("tts") || it.contains("chatterbox") } }
                ?: keys.firstOrNull()
            
            ttsKey?.let { key ->
                _isSpeaking.value = true
                voiceOutputService.speak(
                    apiKey = com.aiva.core.util.KeyStoreManager.decryptString(key.encryptedKey),
                    text = text,
                    voice = _config.value.voiceName,
                    language = _config.value.language,
                    speed = _config.value.speechSpeed,
                    volume = _config.value.volume
                )
            }
        }
    }
    
    fun stopSpeaking() {
        _isSpeaking.value = false
        voiceOutputService.stopSpeaking()
    }
    
    fun interrupt() {
        stopSpeaking()
        cancelListening()
    }
    
    suspend fun updateConfig(newConfig: VoiceConfig) {
        _config.value = newConfig
        apiKeyManager.setBoolean("voice_asr_enabled", newConfig.asrEnabled)
        apiKeyManager.setBoolean("voice_tts_enabled", newConfig.ttsEnabled)
        apiKeyManager.setBoolean("voice_auto_speak", newConfig.autoSpeak)
        apiKeyManager.setString("voice_language", newConfig.language)
        apiKeyManager.setString("voice_voice", newConfig.voiceName)
        apiKeyManager.setString("voice_speed", newConfig.speechSpeed.toString())
        apiKeyManager.setString("voice_volume", newConfig.volume.toString())
        apiKeyManager.setBoolean("voice_interrupt", newConfig.interruptOnUserSpeech)
        apiKeyManager.setString("voice_vad", newConfig.vadSensitivity.toString())
    }
    
    fun getAvailableKeysForAsr(): List<com.aiva.core.security.ApiKeyEntry> {
        return apiKeyManager.getAllKeys()
    }
    
    fun getAvailableKeysForTts(): List<com.aiva.core.security.ApiKeyEntry> {
        return apiKeyManager.getAllKeys()
    }
}