package com.example.soundferry.ui.elements

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.soundferry.R
import com.example.soundferry.ui.theme.ElementConfig
import com.example.soundferry.ui.theme.ThemeEngine

@Composable
fun RecordButton(
    config: ElementConfig,
    isRecording: Boolean,
    onClick: () -> Unit
) {
    val size = (config.style.size ?: 80).dp
    val activeColor = ThemeEngine.parseColor(config.style.colorActive) ?: Color(0xFFD32F2F)
    val idleColor = ThemeEngine.parseColor(config.style.colorIdle) ?: Color(0xFF1976D2)
    val shape = when (config.style.shape) {
        "rounded" -> RoundedCornerShape((config.style.cornerRadius ?: 8).dp)
        "square" -> RoundedCornerShape(0.dp)
        else -> CircleShape
    }

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(size),
        shape = shape,
        containerColor = if (isRecording) activeColor else idleColor
    ) {
        Icon(
            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = if (isRecording) stringResource(R.string.stop_recording) else stringResource(R.string.start_recording),
            modifier = Modifier.size(size * 0.5f),
            tint = Color.White
        )
    }
}
