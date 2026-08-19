package com.aiva.voice.service

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.aiva.core.util.SecureStorage
import com.aiva.core.voice.AsrResult
import com.aiva.core.voice.AudioConfig
import com.aiva.core.voice.VoiceState
import com.aiva.voice.riva.RivaAsrClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceInputService @Inject constructor(
    private val rivaAsrClient: RivaAsrClient
) : LifecycleObserver {
    
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val recordingScope = CoroutineScope(Dispatchers.IO)
    private var currentApiKey: String? = null
    private var currentLanguage = "en-US"
    
    private val _state = MutableStateFlow<VoiceState>(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state
    
    private val _partialResults = MutableStateFlow<String>("")
    val partialResults: StateFlow<String> = _partialResults
    
    private val _finalResults = MutableStateFlow<String>("")
    val finalResults: StateFlow<String> = _finalResults
    
    private val SAMPLE_RATE = 16000
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    ).coerceAtLeast(4096)
    
    fun startListening(
        apiKey: String,
        language: String = "en-US",
        onPartialResult: (String) -> Unit = {},
        onFinalResult: (String) -> Unit = {}
    ) {
        if (isRecording) return
        
        currentApiKey = apiKey
        currentLanguage = language
        isRecording = true
        
        recordingScope.launch {
            val channel = rivaAsrClient.startStreamingRecognition(
                apiKeyEntry = createApiKeyEntry(apiKey),
                audioConfig = AudioConfig(sampleRate = SAMPLE_RATE),
                languageCode = language,
                interimResults = true
            )
            
            try {
                for (result in channel) {
                    if (!isRecording) break
                    
                    if (result.isFinal) {
                        _finalResults.value = result.text
                        onFinalResult(result.text)
                    } else {
                        _partialResults.value = result.text
                        onPartialResult(result.text)
                    }
                }
            } catch (e: Exception) {
                _state.value = VoiceState.ERROR
            }
        }
        
        startAudioRecording()
    }
    
    fun stopListening(): String {
        isRecording = false
        stopAudioRecording()
        rivaAsrClient.stop()
        
        val finalText = _finalResults.value
        if (finalText.isBlank() && _partialResults.value.isNotBlank()) {
            return _partialResults.value
        }
        return finalText
    }
    
    fun cancelListening() {
        isRecording = false
        stopAudioRecording()
        rivaAsrClient.stop()
        _partialResults.value = ""
        _finalResults.value = ""
    }
    
    private fun startAudioRecording() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        ).also { record ->
            record.startRecording()
            
            recordingScope.launch {
                val buffer = ByteArray(BUFFER_SIZE)
                while (isRecording && record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    val read = record.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        // Audio data is sent via the gRPC stream in the client
                        // In a real implementation, you'd send chunks to the streaming RPC
                    }
                }
            }
        }
    }
    
    private fun stopAudioRecording() {
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
    
    private fun createApiKeyEntry(apiKey: String): com.aiva.core.security.ApiKeyEntry {
        return com.aiva.core.security.ApiKeyEntry(
            id = "voice_input",
            name = "Voice Input",
            keyHash = "",
            encryptedKey = com.aiva.core.util.KeyStoreManager.encryptString(apiKey),
            models = emptyList()
        )
    }
    
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        cancelListening()
        recordingScope.cancel()
    }
}