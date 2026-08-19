package com.aiva.voice.riva

import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.voice.TtsRequest
import com.aiva.core.voice.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RivaTtsClient constructor() {

    private val _state = MutableStateFlow(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state

    suspend fun synthesize(
        apiKeyEntry: ApiKeyEntry,
        request: TtsRequest
    ): ByteArray? {
        if (apiKeyEntry.encryptedKey.isBlank() || request.text.isBlank()) return null
        _state.value = VoiceState.SPEAKING
        _state.value = VoiceState.IDLE
        return ByteArray(0)
    }

    suspend fun synthesizeStreaming(
        apiKeyEntry: ApiKeyEntry,
        request: TtsRequest
    ): ByteArray? = synthesize(apiKeyEntry, request)

    fun stop() {
        _state.value = VoiceState.IDLE
    }
}
