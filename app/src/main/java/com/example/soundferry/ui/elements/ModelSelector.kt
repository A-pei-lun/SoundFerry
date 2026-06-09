package com.example.soundferry.ui.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.soundferry.R
import com.example.soundferry.ui.theme.ElementConfig

/**
 * 模型选择器
 * 显示可用模型列表，点击切换
 */
@Composable
fun ModelSelector(
    config: ElementConfig,
    currentModel: String = "",
    availableModels: List<String> = emptyList(),
    onSelectModel: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    val displayName = currentModel
        .replace("vosk-model-", "")
        .replace("-cn-", "中文版 ")
        .replace("-small-", "小 ")

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.Memory, contentDescription = stringResource(R.string.model_selector),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        availableModels.forEach { model ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = "${if (model == currentModel) "✓ " else ""}${model
                            .replace("vosk-model-", "")
                            .replace("-cn-", "中文版 ")
                            .replace("-small-", "小 ")}"
                    )
                },
                onClick = {
                    if (model != currentModel) {
                        onSelectModel(model)
                    }
                    expanded = false
                }
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        Text(
            text = stringResource(R.string.model_switch_warning),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
