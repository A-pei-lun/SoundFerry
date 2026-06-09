package com.example.soundferry.ui.elements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import com.example.soundferry.R
import com.example.soundferry.ui.theme.ElementConfig
import com.example.soundferry.ui.theme.ThemeEngine

@Composable
fun TextDisplay(
    modifier: Modifier = Modifier,
    config: ElementConfig,
    isRecording: Boolean,
    voskReady: Boolean,
    currentText: String,
    finalText: String,
    translatedText: String,
    isTranslating: Boolean,
    showTranslation: Boolean
) {
    val scrollState = rememberScrollState()
    val cornerRadius = (config.style.cornerRadius ?: 16).dp
    val textSize = (config.style.textSize ?: 18).sp
    val bgColor = ThemeEngine.parseColor(config.style.bgColor)

    // 自动滚动
    LaunchedEffect(currentText, finalText) {
        if (currentText.isNotBlank() || finalText.isNotBlank()) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = bgColor ?: MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Vosk 加载中
            if (!voskReady) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.model_loading), style = MaterialTheme.typography.bodySmall)
            }

            if (isRecording) {
                // 录音中：实时文字
                Box(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentText.ifBlank { stringResource(R.string.listening) },
                        fontSize = textSize,
                        color = if (currentText.isNotBlank())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (finalText.isNotBlank()) {
                // 停止后：最终文字 + 翻译
                Column(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
                    Text(
                        text = finalText,
                        fontSize = textSize,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (showTranslation) {
                        HorizontalDivider(modifier = Modifier.padding(bottom = 8.dp))
                        Text(
                            text = stringResource(R.string.english_translation),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isTranslating) {
                            Text(
                                text = stringResource(R.string.translating),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = translatedText.ifBlank { stringResource(R.string.translation_failed_text) },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            } else {
                // 初始状态
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.click_to_record),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
