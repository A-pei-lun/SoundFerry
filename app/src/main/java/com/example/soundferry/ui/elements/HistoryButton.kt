package com.example.soundferry.ui.elements

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.soundferry.R
import com.example.soundferry.ui.theme.ElementConfig

@Composable
fun HistoryButton(
    config: ElementConfig,
    onClick: () -> Unit
) {
    val iconSize = (config.style.iconSize ?: 24).dp

    IconButton(onClick = onClick) {
        Icon(
            Icons.Default.History,
            contentDescription = stringResource(R.string.history),
            modifier = Modifier.size(iconSize),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
