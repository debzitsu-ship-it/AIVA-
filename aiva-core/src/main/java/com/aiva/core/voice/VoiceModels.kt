package com.aiva.core.voice

import kotlinx.serialization.Serializable

@Serializable
data class VoiceConfig(
    val asrEnabled: Boolean = true,
    val ttsEnabled: Boolean = true,
    val autoSpeak: Boolean = false,
    val language: String = "en-US",
    val voiceName: String = "Chatterbox-Multilingual.en-US.Male",
    val speechSpeed: Float = 1.0f,
    val volume: Float = 1.0f,
    val interruptOnUserSpeech: Boolean = true,
    val vadSensitivity: Float = 0.5f
)

@Serializable
data class AsrResult(
    val text: String,
    val confidence: Float,
    val language: String?,
    val isFinal: Boolean,
    val alternatives: List<String> = emptyList()
)

@Serializable
data class TtsRequest(
    val text: String,
    val voice: String = "Chatterbox-Multilingual.en-US.Male",
    val language: String = "en-US",
    val speed: Float = 1.0f,
    val volume: Float = 1.0f
)

@Serializable
data class AudioConfig(
    val sampleRate: Int = 16000,
    val channelCount: Int = 1,
    val encoding: AudioEncoding = AudioEncoding.PCM_16BIT
)

@Serializable
enum class AudioEncoding {
    PCM_16BIT, OPUS, FLAC
}

@Serializable
enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    INTERRUPTED,
    ERROR
}

@Serializable
data class VoiceCommand(
    val action: VoiceAction,
    val text: String? = null
)

enum class VoiceAction {
    START_LISTENING,
    STOP_LISTENING,
    SPEAK,
    STOP_SPEAKING,
    INTERRUPT,
    SET_CONFIG
}