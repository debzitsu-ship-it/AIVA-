package com.aiva.voice.riva

import com.aiva.core.security.ApiKeyEntry
import com.aiva.core.util.KeyStoreManager
import com.aiva.core.voice.AsrResult
import com.aiva.core.voice.AudioConfig
import com.aiva.core.voice.VoiceState
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Metadata
import io.grpc.okhttp.OkHttpChannelProvider
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import nvidia.riva.asr.RecognitionConfig
import nvidia.riva.asr.RivaSpeechRecognitionGrpcKt
import nvidia.riva.asr.StreamingRecognizeRequest
import nvidia.riva.asr.StreamingRecognizeResponse
import nvidia.riva.asr.StreamingRecognitionConfig
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RivaAsrClient @Inject constructor() {
    
    private var channel: ManagedChannel? = null
    private var currentApiKey: String? = null
    
    private val _state = MutableStateFlow<VoiceState>(VoiceState.IDLE)
    val state: StateFlow<VoiceState> = _state
    
    private val SERVER_HOST = "grpc.nvcf.nvidia.com"
    private val SERVER_PORT = 443
    private val FUNCTION_ID = "b702f636-f60c-4a3d-a6f4-f3568c13bd7d"
    
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
    
    fun startStreamingRecognition(
        apiKeyEntry: ApiKeyEntry,
        audioConfig: AudioConfig = AudioConfig(),
        languageCode: String = "en-US",
        interimResults: Boolean = true
    ): ReceiveChannel<AsrResult> {
        val decryptedKey = KeyStoreManager.decryptString(apiKeyEntry.encryptedKey)
        val grpcChannel = getOrCreateChannel(decryptedKey)
        val stub = RivaSpeechRecognitionGrpcKt.RivaSpeechRecognitionCoroutineStub(grpcChannel)
        
        val resultChannel = Channel<AsrResult>(Channel.UNLIMITED)
        
        CoroutineScope(Dispatchers.IO).launch {
            _state.value = VoiceState.LISTENING
            
            val config = RecognitionConfig.newBuilder()
                .setEncoding("LINEAR16")
                .setSampleRateHertz(audioConfig.sampleRate)
                .setLanguageCode(languageCode)
                .setMaxAlternatives(1)
                .setEnableAutomaticPunctuation(true)
                .setModel("whisper-large-v3")
                .build()
            
            val streamingConfig = StreamingRecognitionConfig.newBuilder()
                .setConfig(config)
                .setInterimResults(interimResults)
                .setSingleUtterance(false)
                .build()
            
            val requestStream = stub.streamingRecognize()
            
            // Send initial config
            requestStream.send(StreamingRecognizeRequest.newBuilder()
                .setStreamingConfig(streamingConfig)
                .build())
            
            // Receive responses
            val responseJob = CoroutineScope(Dispatchers.IO).launch {
                try {
                    for (response in requestStream) {
                        processResponse(response, resultChannel)
                    }
                } catch (e: Exception) {
                    resultChannel.close(e)
                }
            }
            
            // Keep reference to cancel later
            // In real implementation, store this for cancellation
        }
        
        return resultChannel
    }
    
    private fun processResponse(
        response: StreamingRecognizeResponse,
        channel: Channel<AsrResult>
    ) {
        for (result in response.resultsList) {
            val alternative = result.alternativesList.firstOrNull() ?: continue
            val asrResult = AsrResult(
                text = alternative.transcript,
                confidence = alternative.confidence,
                language = null,
                isFinal = result.isFinal,
                alternatives = result.alternativesList.map { it.transcript }
            )
            channel.trySend(asrResult)
        }
    }
    
    suspend fun recognizeOnce(
        apiKeyEntry: ApiKeyEntry,
        audioData: ByteArray,
        audioConfig: AudioConfig = AudioConfig(),
        languageCode: String = "en-US"
    ): AsrResult? {
        val decryptedKey = KeyStoreManager.decryptString(apiKeyEntry.encryptedKey)
        val grpcChannel = getOrCreateChannel(decryptedKey)
        val stub = RivaSpeechRecognitionGrpcKt.RivaSpeechRecognitionCoroutineStub(grpcChannel)
        
        val config = RecognitionConfig.newBuilder()
            .setEncoding("LINEAR16")
            .setSampleRateHertz(audioConfig.sampleRate)
            .setLanguageCode(languageCode)
            .setMaxAlternatives(1)
            .setEnableAutomaticPunctuation(true)
            .setModel("whisper-large-v3")
            .build()
        
        val request = nvidia.riva.asr.RecognizeRequest.newBuilder()
            .setConfig(config)
            .setAudioContent(com.google.protobuf.ByteString.copyFrom(audioData))
            .build()
        
        return try {
            val response = stub.recognize(request)
            val alternative = response.resultsList.firstOrNull()?.alternativesList?.firstOrNull()
            alternative?.let {
                AsrResult(
                    text = it.transcript,
                    confidence = it.confidence,
                    language = languageCode,
                    isFinal = true,
                    alternatives = response.resultsList.flatMap { it.alternativesList.map { it.transcript } }
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun stop() {
        _state.value = VoiceState.IDLE
        closeChannel()
    }
    
    private fun closeChannel() {
        channel?.shutdown()
        channel?.awaitTermination(5, TimeUnit.SECONDS)
        channel = null
        currentApiKey = null
    }
    
    companion object {
        // MetadataUtils is from io.grpc:grpc-okhttp
        @Suppress("UNUSED_PARAMETER")
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