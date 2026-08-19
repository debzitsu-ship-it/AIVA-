package com.aiva.core.voice

import com.aiva.core.voice.AsrResult
import com.aiva.core.voice.AudioConfig
import com.aiva.core.voice.AudioEncoding
import com.aiva.core.voice.TtsRequest
import com.aiva.core.voice.VoiceConfig
import com.aiva.core.voice.VoiceState
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VoiceTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @Test
    fun testVoiceConfigSerialization() {
        val config = VoiceConfig(
            asrEnabled = true,
            ttsEnabled = true,
            autoSpeak = false,
            language = "en-US",
            voiceName = "Chatterbox-Multilingual.en-US.Male",
            speechSpeed = 1.0f,
            volume = 1.0f,
            interruptOnUserSpeech = true,
            vadSensitivity = 0.5f
        )
        
        val jsonStr = json.encodeToString(config)
        val decoded = json.decodeFromString(VoiceConfig.serializer(), jsonStr)
        assertEquals(config, decoded)
    }
    
    @Test
    fun testAsrResultSerialization() {
        val result = AsrResult(
            text = "Hello world",
            confidence = 0.98f,
            language = "en-US",
            isFinal = true,
            alternatives = listOf("Hello world", "Hello word")
        )
        
        val jsonStr = json.encodeToString(result)
        val decoded = json.decodeFromString(AsrResult.serializer(), jsonStr)
        assertEquals(result, decoded)
    }
    
    @Test
    fun testTtsRequestSerialization() {
        val request = TtsRequest(
            text = "Hello",
            voice = "Chatterbox-Multilingual.en-US.Male",
            language = "en-US",
            speed = 1.2f,
            volume = 0.8f
        )
        
        val jsonStr = json.encodeToString(request)
        val decoded = json.decodeFromString(TtsRequest.serializer(), jsonStr)
        assertEquals(request, decoded)
    }
    
    @Test
    fun testAudioConfigDefaults() {
        val config = AudioConfig()
        assertEquals(16000, config.sampleRate)
        assertEquals(1, config.channelCount)
        assertEquals(AudioEncoding.PCM_16BIT, config.encoding)
    }
    
    @Test
    fun testVoiceStateValues() {
        val states = VoiceState.values()
        assertEquals(6, states.size)
        assertTrue(states.contains(VoiceState.IDLE))
        assertTrue(states.contains(VoiceState.LISTENING))
        assertTrue(states.contains(VoiceState.PROCESSING))
        assertTrue(states.contains(VoiceState.SPEAKING))
        assertTrue(states.contains(VoiceState.INTERRUPTED))
        assertTrue(states.contains(VoiceState.ERROR))
    }
}