package com.aiva.voice.service

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.aiva.core.util.KeyStoreManager
import com.aiva.core.voice.TtsRequest
import com.aiva.core.voice.VoiceState
import com.aiva.voice.riva.RivaTtsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceOutputService @Inject constructor(
    private val rivaTtsClient: RivaTtsClient
) {
    
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private val playbackScope = CoroutineScope(Dispatchers.IO)
    
    private val _state = MutableStateFlow<VoiceState>(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state
    
    private val SAMPLE_RATE = 22050
    private val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val BUFFER_SIZE = AudioTrack.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
    ).coerceAtLeast(4096)
    
    suspend fun speak(
        apiKey: String,
        text: String,
        voice: String = "Chatterbox-Multilingual.en-US.Male",
        language: String = "en-US",
        speed: Float = 1.0f,
        volume: Float = 1.0f
    ): Boolean {
        if (text.isBlank()) return false
        
        stopSpeaking()
        
        val request = TtsRequest(
            text = text,
            voice = voice,
            language = language,
            speed = speed,
            volume = volume
        )
        
        val apiKeyEntry = createApiKeyEntry(apiKey)
        
        return try {
            val audioData = rivaTtsClient.synthesize(apiKeyEntry, request)
            audioData?.let { playAudio(it) } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    fun speakStreaming(
        apiKey: String,
        text: String,
        voice: String = "Chatterbox-Multilingual.en-US.Male",
        language: String = "en-US",
        speed: Float = 1.0f,
        volume: Float = 1.0f
    ) {
        if (text.isBlank()) return
        
        stopSpeaking()
        
        val request = TtsRequest(
            text = text,
            voice = voice,
            language = language,
            speed = speed,
            volume = volume
        )
        
        val apiKeyEntry = createApiKeyEntry(apiKey)
        
        playbackScope.launch {
            val audioData = rivaTtsClient.synthesizeStreaming(apiKeyEntry, request)
            audioData?.let { playAudio(it) }
        }
    }
    
    fun stopSpeaking() {
        isPlaying = false
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        rivaTtsClient.stop()
        _state.value = VoiceState.IDLE
    }
    
    private fun playAudio(audioData: ByteArray): Boolean {
        isPlaying = true
        
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AUDIO_FORMAT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNEL_CONFIG)
                    .build()
            )
            .setBufferSizeInBytes(BUFFER_SIZE)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        
        audioTrack?.play()
        
        try {
            var offset = 0
            while (isPlaying && offset < audioData.size) {
                val chunkSize = minOf(BUFFER_SIZE, audioData.size - offset)
                audioTrack?.write(audioData, offset, chunkSize, AudioTrack.WRITE_BLOCKING)
                offset += chunkSize
            }
            audioTrack?.flush()
            return true
        } catch (e: Exception) {
            return false
        } finally {
            stopSpeaking()
        }
    }
    
    private fun createApiKeyEntry(apiKey: String): com.aiva.core.security.ApiKeyEntry {
        return com.aiva.core.security.ApiKeyEntry(
            id = "voice_output",
            name = "Voice Output",
            keyHash = "",
            encryptedKey = KeyStoreManager.encryptString(apiKey),
            models = emptyList()
        )
    }
}