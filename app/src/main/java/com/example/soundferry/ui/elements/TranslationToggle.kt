package com.example.soundferry.ui.elements

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.soundferry.R
import com.example.soundferry.ui.theme.ElementConfig
import com.example.soundferry.ui.theme.ThemeEngine

@Composable
fun TranslationToggle(
    config: ElementConfig,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    val activeColor = ThemeEngine.parseColor(config.style.colorActive)

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Translate,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (checked && activeColor != null) activeColor
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(stringResource(R.string.translation_toggle), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
