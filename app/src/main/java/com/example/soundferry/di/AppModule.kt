package com.example.soundferry.di

import android.content.Context
import com.example.soundferry.audio.AudioRecorder
import com.example.soundferry.audio.engine.EngineManager
import com.example.soundferry.audio.engine.SherpaEngine
import com.example.soundferry.audio.engine.VoskEngine
import com.example.soundferry.data.HistoryRepository
import com.example.soundferry.data.ModelImporter
import com.example.soundferry.data.SettingsRepository
import com.example.soundferry.translate.MLKitTranslator
import com.example.soundferry.translate.Translator
import com.example.soundferry.ui.MainViewModel
import com.example.soundferry.ui.theme.ThemeRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {

    // 引擎层
    singleOf(::VoskEngine)
    singleOf(::SherpaEngine)
    singleOf(::EngineManager)

    // 翻译
    singleOf(::MLKitTranslator) bind Translator::class

    // 音频录制
    singleOf(::AudioRecorder)

    // 历史记录
    single { HistoryRepository(get()) }

    // 主题
    single { ThemeRepository(get()) }

    // 设置持久化
    single { SettingsRepository(get()) }

    // 模型导入
    single { ModelImporter(get()) }

    // ViewModel（显式注入，避免 Koin 构造函数匹配问题）
    viewModel { MainViewModel(get(), get(), get(), get(), get(), get(), get()) }
}
