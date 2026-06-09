package com.example.soundferry

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.soundferry.data.settingsDataStore
import com.example.soundferry.ui.MainScreen
import com.example.soundferry.ui.theme.SoundFerryTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        applyAppLocale()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SoundFerryTheme {
                MainScreen()
            }
        }
    }

    private fun applyAppLocale() {
        try {
            val lang = runBlocking {
                settingsDataStore.data.first()[stringPreferencesKey("app_language")] ?: "zh"
            }
            if (lang != "zh") {
                val config = Configuration(baseContext.resources.configuration)
                config.setLocale(java.util.Locale(lang))
                baseContext.createConfigurationContext(config)
                resources.updateConfiguration(config, resources.displayMetrics)
            }
        } catch (_: Exception) { }
    }
}
