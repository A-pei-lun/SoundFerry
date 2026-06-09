package com.example.soundferry.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.example.soundferry.audio.engine.EngineManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

/**
 * 音频录制器
 * 通过 EngineManager 调用当前 ASR 引擎
 */
class AudioRecorder(
    private val engineManager: EngineManager
) {
    private var audioRecord: AudioRecord? = null
    private val lock = Any()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val sampleRate = 16000

    suspend fun start(
        onPartial: (String) -> Unit
    ) = withContext(Dispatchers.IO) {
        if (_isRecording.value) return@withContext

        val minBufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = maxOf(minBufferSize * 2, 3200)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord 初始化失败")
            return@withContext
        }

        audioRecord?.startRecording()
        _isRecording.value = true

        val buffer = ByteArray(bufferSize)
        while (isActive && _isRecording.value) {
            val bytesRead = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (bytesRead > 0) {
                synchronized(lock) {
                    engineManager.feedAudioChunk(buffer, bytesRead)
                    engineManager.getPartialResult()?.let { partial ->
                        if (partial.isNotBlank()) onPartial(partial)
                    }
                }
            }
        }
    }

    fun stop(): String {
        _isRecording.value = false
        try { audioRecord?.stop() } catch (_: Exception) {}
        audioRecord?.release()
        audioRecord = null

        synchronized(lock) {
            val finalText = engineManager.getFinalResult()
            engineManager.reset()
            return finalText ?: ""
        }
    }

    companion object {
        private const val TAG = "AudioRecorder"
    }
}
