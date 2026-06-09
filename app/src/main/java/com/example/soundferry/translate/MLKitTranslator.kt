package com.example.soundferry.translate

import android.util.Log
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * ML Kit 翻译实现
 * 中文 → 英文，离线翻译
 */
class MLKitTranslator : Translator {

    private var translator: com.google.mlkit.nl.translate.Translator? = null

    /** 模型下载状态 */
    var isModelDownloaded = false
        private set

    /**
     * 初始化翻译器（首次需要下载模型 ~30-50MB）
     */
    suspend fun init(): Boolean {
        return try {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.CHINESE)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()

            translator = Translation.getClient(options)

            translator?.let { t ->
                t.downloadModelIfNeeded().await()
                isModelDownloaded = true
                Log.d(TAG, "ML Kit 翻译模型就绪")
                true
            } ?: false
        } catch (e: Exception) {
            Log.e(TAG, "ML Kit 初始化失败", e)
            false
        }
    }

    /**
     * 下载翻译模型（带进度监听）
     */
    fun downloadModel(): Flow<Boolean> = callbackFlow {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.CHINESE)
            .setTargetLanguage(TranslateLanguage.ENGLISH)
            .build()

        val t = Translation.getClient(options)
        translator = t

        try {
            // 30 秒超时，防止 Google 服务器不通卡死
            withTimeout(30_000) {
                t.downloadModelIfNeeded().await()
            }
            isModelDownloaded = true
            trySend(true)
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Log.e(TAG, "模型下载超时（Google 服务器可能不可用）")
            trySend(false)
        } catch (e: Exception) {
            Log.e(TAG, "模型下载失败", e)
            trySend(false)
        }
        close()
        awaitClose { }
    }

    /**
     * 翻译文本
     */
    override suspend fun translate(text: String, sourceLang: String, targetLang: String): String {
        if (!isModelDownloaded) {
            Log.w(TAG, "翻译模型未就绪")
            return ""
        }
        return try {
            translator?.translate(text)?.await() ?: ""
        } catch (e: Exception) {
            Log.e(TAG, "翻译失败", e)
            ""
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        translator?.close()
        translator = null
    }

    companion object {
        private const val TAG = "MLKitTranslator"
    }
}
