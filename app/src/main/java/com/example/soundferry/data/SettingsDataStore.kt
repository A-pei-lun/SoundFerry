package com.example.soundferry.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

/**
 * 全局唯一的 DataStore 实例
 * MainActivity 和 SettingsRepository 都使用此实例
 */
val Context.settingsDataStore by preferencesDataStore("soundferry_settings")
