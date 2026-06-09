package com.example.soundferry.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 设置持久化仓库
 * 保存主题选择、液化玻璃开关等用户偏好
 */
class SettingsRepository(private val context: Context) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("current_theme")
        private val GLASS_KEY = booleanPreferencesKey("liquid_glass")
        private val LANG_KEY = stringPreferencesKey("app_language")
    }

    /** 当前主题文件名 */
    val currentThemeFileName: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: "极简黑.json"
    }

    /** 液化玻璃开关 */
    val liquidGlassEnabled: Flow<Boolean> = context.settingsDataStore.data.map { prefs ->
        prefs[GLASS_KEY] ?: false
    }

    /** 语言设置（zh/en） */
    val appLanguage: Flow<String> = context.settingsDataStore.data.map { prefs ->
        prefs[LANG_KEY] ?: "zh"
    }

    /** 保存主题选择 */
    suspend fun saveTheme(fileName: String) {
        context.settingsDataStore.edit { prefs -> prefs[THEME_KEY] = fileName }
    }

    /** 保存液化玻璃开关 */
    suspend fun saveLiquidGlass(enabled: Boolean) {
        context.settingsDataStore.edit { prefs -> prefs[GLASS_KEY] = enabled }
    }

    /** 保存语言设置 */
    suspend fun saveLanguage(lang: String) {
        context.settingsDataStore.edit { prefs -> prefs[LANG_KEY] = lang }
    }
}
