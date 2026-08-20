package com.aiva.voice.riva

import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.voice.AsrResult
import com.aiva.core.voice.AudioConfig
import com.aiva.core.voice.VoiceState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RivaAsrClient constructor() {

    private val _state = MutableStateFlow(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state

    fun startStreamingRecognition(
        apiKeyEntry: ApiKeyEntry,
        audioConfig: AudioConfig = AudioConfig(),
        languageCode: String = "en-US",
        interimResults: Boolean = true
    ): ReceiveChannel<AsrResult> {
        _state.value = VoiceState.LISTENING
        return Channel<AsrResult>(Channel.UNLIMITED).also { it.close() }
    }

    suspend fun recognizeOnce(
        apiKeyEntry: ApiKeyEntry,
        audioData: ByteArray,
        audioConfig: AudioConfig = AudioConfig(),
        languageCode: String = "en-US"
    ): AsrResult? {
        if (apiKeyEntry.encryptedKey.isBlank() || audioData.isEmpty()) return null
        return AsrResult(
            text = "",
            confidence = 0f,
            language = languageCode,
            isFinal = true
        )
    }

    fun stop() {
        _state.value = VoiceState.IDLE
    }
}
