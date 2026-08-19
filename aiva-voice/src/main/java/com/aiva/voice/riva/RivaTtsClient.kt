package com.aiva.voice.riva

import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.util.KeyStoreManager
import com.aiva.core.voice.TtsRequest
import com.aiva.core.voice.VoiceState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.okhttp.OkHttpChannelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nvidia.riva.tts.AudioConfig
import nvidia.riva.tts.AudioEncoding
import nvidia.riva.tts.RivaSpeechSynthesisGrpcKt
import nvidia.riva.tts.SynthesizeSpeechRequest
import nvidia.riva.tts.SynthesizeSpeechResponse
import nvidia.riva.tts.TextInput
import nvidia.riva.tts.VoiceSelectionParams
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RivaTtsClient @Inject constructor() {
    
    private var channel: ManagedChannel? = null
    private var currentApiKey: String? = null
    
    private val _state = MutableStateFlow<VoiceState>(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state
    
    private val SERVER_HOST = "grpc.nvcf.nvidia.com"
    private val SERVER_PORT = 443
    private val FUNCTION_ID = "ddacc747-1269-4fab-bfd9-8f593dead106"
    
    private fun getOrCreateChannel(apiKey: String): ManagedChannel {
        if (channel != null && currentApiKey == apiKey) {
            return channel!!
        }
        
        closeChannel()
        
        val metadata = Metadata()
        metadata.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer $apiKey")
        metadata.put(Metadata.Key.of("function-id", Metadata.ASCII_STRING_MARSHALLER), FUNCTION_ID)
        
        channel = OkHttpChannelProvider()
            .builderForAddress(SERVER_HOST, SERVER_PORT)
            .intercept(MetadataUtils.newAttachHeadersInterceptor(metadata))
            .build()
        
        currentApiKey = apiKey
        return channel!!
    }
    
    suspend fun synthesize(
        apiKeyEntry: ApiKeyEntry,
        request: TtsRequest
    ): ByteArray? {
        val decryptedKey = KeyStoreManager.decryptString(apiKeyEntry.encryptedKey)
        val grpcChannel = getOrCreateChannel(decryptedKey)
        val stub = RivaSpeechSynthesisGrpcKt.RivaSpeechSynthesisCoroutineStub(grpcChannel)
        
        _state.value = VoiceState.SPEAKING
        
        return try {
            val audioConfig = AudioConfig.newBuilder()
                .setAudioEncoding(AudioEncoding.LINEAR16)
                .setSpeakingRate(request.speed)
                .setVolumeGainDb(20f * Math.log10(request.volume.coerceAtLeast(0.01f)))
                .setSampleRateHertz(22050)
                .build()
            
            val voiceParams = VoiceSelectionParams.newBuilder()
                .setName(request.voice)
                .setLanguageCode(request.language)
                .setSsmlGender(VoiceSelectionParams.SsmlVoiceGender.MALE)
                .build()
            
            val textInput = TextInput.newBuilder()
                .setText(request.text)
                .build()
            
            val grpcRequest = SynthesizeSpeechRequest.newBuilder()
                .setText(textInput)
                .setVoice(voiceParams)
                .setAudioConfig(audioConfig)
                .build()
            
            val response = stub.synthesize(grpcRequest)
            response.audioContent.toByteArray()
        } catch (e: Exception) {
            null
        } finally {
            _state.value = VoiceState.IDLE
        }
    }
    
    suspend fun synthesizeStreaming(
        apiKeyEntry: ApiKeyEntry,
        request: TtsRequest
    ): ByteArray? {
        val decryptedKey = KeyStoreManager.decryptString(apiKeyEntry.encryptedKey)
        val grpcChannel = getOrCreateChannel(decryptedKey)
        val stub = RivaSpeechSynthesisGrpcKt.RivaSpeechSynthesisCoroutineStub(grpcChannel)
        
        _state.value = VoiceState.SPEAKING
        
        val audioConfig = AudioConfig.newBuilder()
            .setAudioEncoding(AudioEncoding.LINEAR16)
            .setSpeakingRate(request.speed)
            .setVolumeGainDb(20f * Math.log10(request.volume.coerceAtLeast(0.01f)))
            .setSampleRateHertz(22050)
            .build()
        
        val voiceParams = VoiceSelectionParams.newBuilder()
            .setName(request.voice)
            .setLanguageCode(request.language)
            .setSsmlGender(VoiceSelectionParams.SsmlVoiceGender.MALE)
            .build()
        
        val textInput = TextInput.newBuilder()
            .setText(request.text)
            .build()
        
        val grpcRequest = SynthesizeSpeechRequest.newBuilder()
            .setText(textInput)
            .setVoice(voiceParams)
            .setAudioConfig(audioConfig)
            .build()
        
        val response = stub.synthesizeOnline(grpcRequest)
        val buffer = java.io.ByteArrayOutputStream()
        
        try {
            for (chunk in response) {
                buffer.write(chunk.audioContent.toByteArray())
            }
            return buffer.toByteArray()
        } catch (e: Exception) {
            null
        } finally {
            _state.value = VoiceState.IDLE
        }
    }
    
    fun stop() {
        _state.value = VoiceState.IDLE
    }
    
    private fun closeChannel() {
        channel?.shutdown()
        channel?.awaitTermination(5, TimeUnit.SECONDS)
        channel = null
        currentApiKey = null
    }
    
    companion object {
        private fun MetadataUtils.newAttachHeadersInterceptor(metadata: Metadata): io.grpc.ClientInterceptor {
            return io.grpc.ClientInterceptors.headerTransformer { headers ->
                metadata.keys.forEach { key ->
                    val values = metadata.getAll(key)
                    values.forEach { value ->
                        headers.add(key, value)
                    }
                }
            }
        }
    }
}