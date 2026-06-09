package com.example.soundferry.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.soundferry.ui.elements.HistoryButton
import com.example.soundferry.ui.elements.ModelSelector
import com.example.soundferry.ui.elements.RecordButton
import com.example.soundferry.ui.elements.SettingsButton
import com.example.soundferry.ui.elements.TextDisplay
import com.example.soundferry.ui.elements.TranslationToggle
import com.example.soundferry.ui.MainViewModel

object ThemeEngine {

    fun anchorToAlignment(anchor: Anchor): Alignment {
        return when (anchor) {
            Anchor.TOP_LEFT -> Alignment.TopStart
            Anchor.TOP_CENTER -> Alignment.TopCenter
            Anchor.TOP_RIGHT -> Alignment.TopEnd
            Anchor.CENTER_LEFT -> Alignment.CenterStart
            Anchor.CENTER -> Alignment.Center
            Anchor.CENTER_RIGHT -> Alignment.CenterEnd
            Anchor.BOTTOM_LEFT -> Alignment.BottomStart
            Anchor.BOTTOM_CENTER -> Alignment.BottomCenter
            Anchor.BOTTOM_RIGHT -> Alignment.BottomEnd
        }
    }

    fun marginModifier(pos: ElementPosition): Modifier {
        return Modifier.padding(
            start = pos.marginLeft.dp,
            top = pos.marginTop.dp,
            end = pos.marginRight.dp,
            bottom = pos.marginBottom.dp
        )
    }

    fun parseColor(hex: String?): Color? {
        if (hex == null) return null
        return try {
            val h = hex.removePrefix("#")
            val argb = h.toLong(16)
            when (h.length) {
                // #RRGGBB
                6 -> Color(
                    red = ((argb shr 16) and 0xFF).toFloat() / 255f,
                    green = ((argb shr 8) and 0xFF).toFloat() / 255f,
                    blue = (argb and 0xFF).toFloat() / 255f,
                    alpha = 1f
                )
                // #AARRGGBB
                8 -> Color(
                    red = ((argb shr 16) and 0xFF).toFloat() / 255f,
                    green = ((argb shr 8) and 0xFF).toFloat() / 255f,
                    blue = (argb and 0xFF).toFloat() / 255f,
                    alpha = ((argb shr 24) and 0xFF).toFloat() / 255f
                )
                else -> null
            }
        } catch (_: Exception) { null }
    }

    @Composable
    fun RenderElement(
        config: ElementConfig,
        isRecording: Boolean,
        voskReady: Boolean,
        currentText: String,
        finalText: String,
        translatedText: String,
        isTranslating: Boolean,
        showTranslation: Boolean,
        currentModel: String = "",
        availableModels: List<String> = emptyList(),
        onSelectModel: (String) -> Unit = {},
        onRecordToggle: () -> Unit,
        onTranslationToggle: (Boolean) -> Unit,
        onOpenHistory: () -> Unit,
        onOpenSettings: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        when (config.type) {
            ElementType.RECORD_BUTTON -> RecordButton(
                config = config, isRecording = isRecording, onClick = onRecordToggle
            )
            ElementType.TEXT_DISPLAY -> TextDisplay(
                modifier = modifier, config = config, isRecording = isRecording,
                voskReady = voskReady, currentText = currentText, finalText = finalText,
                translatedText = translatedText, isTranslating = isTranslating,
                showTranslation = showTranslation
            )
            ElementType.SETTINGS_BUTTON -> SettingsButton(
                config = config, onClick = onOpenSettings
            )
            ElementType.TRANSLATION_TOGGLE -> TranslationToggle(
                config = config, checked = showTranslation,
                onToggle = onTranslationToggle
            )
            ElementType.HISTORY_BUTTON -> HistoryButton(
                config = config, onClick = onOpenHistory
            )
            ElementType.MODEL_SELECTOR -> ModelSelector(
                config = config, currentModel = currentModel,
                availableModels = availableModels, onSelectModel = onSelectModel
            )
            else -> {}
        }
    }
}
