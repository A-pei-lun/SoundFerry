package com.example.soundferry.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.soundferry.R
import com.example.soundferry.data.HistoryItem
import com.example.soundferry.ui.experimental.LiquidGlassEffect
import com.example.soundferry.ui.theme.ElementType
import com.example.soundferry.ui.theme.ThemeEngine
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

// ==================== 主入口 ====================

@Composable
fun MainScreen(viewModel: MainViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle(initialValue = emptyList())
    var showHistory by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // 液化玻璃
    LiquidGlassEffect(enabled = state.liquidGlassEnabled)

    // 麦克风权限
    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.toggleRecording()
        else viewModel.showError(context.getString(R.string.mic_permission_denied))
    }

    fun checkAndRecord() {
        val perm = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED) {
            viewModel.toggleRecording()
        } else {
            micLauncher.launch(perm)
        }
    }

    if (state.showSettings) {
        SettingsScreen(
            themes = state.themes,
            currentThemeFileName = state.currentThemeFileName,
            liquidGlassEnabled = state.liquidGlassEnabled,
            currentEngine = state.currentEngine,
            availableEngines = state.availableEngines,
            pendingModels = state.pendingModels,
            availableModels = state.availableModels,
            onThemeSelected = { viewModel.selectTheme(it) },
            onLiquidGlassToggle = { viewModel.toggleLiquidGlass() },
            onSwitchEngine = { viewModel.switchEngine(it) },
            onImportModel = { viewModel.importModel(it) },
            onDeleteZip = { viewModel.deletePendingZip(it) },
            currentLanguage = state.appLanguage,
            onSwitchLanguage = { viewModel.switchLanguage(it) },
            onBack = { viewModel.closeSettings() }
        )
    } else {
        // ===== 主题驱动的主界面 =====
        ThemePage(
            state = state,
            checkAndRecord = { checkAndRecord() },
            onTranslationToggle = { viewModel.toggleTranslation() },
            onOpenHistory = { showHistory = true },
            onOpenSettings = { viewModel.openSettings() },
            onSelectModel = { viewModel.switchModel(it) }
        )
    }

    // 历史弹窗
    if (showHistory) {
        HistoryDialog(history, onDismiss = { showHistory = false },
            onClear = { viewModel.clearHistory() })
    }
}

// ==================== 主题驱动页面 ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemePage(
    state: UiState,
    checkAndRecord: () -> Unit,
    onTranslationToggle: (Boolean) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectModel: (String) -> Unit = {}
) {
    val theme = state.themes.find { it.fileName == state.currentThemeFileName }
        ?: state.themes.firstOrNull()

    Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars)) {
        // ===== 背景层：文字显示（整屏填充） =====
        val textEl = theme?.elements?.find { it.type == ElementType.TEXT_DISPLAY }
        if (textEl != null) {
            val textMargins = ThemeEngine.marginModifier(textEl.position)
            ThemeEngine.RenderElement(
                config = textEl,
                isRecording = state.isRecording,
                voskReady = state.voskReady,
                currentText = state.currentText,
                finalText = state.finalText,
                translatedText = state.translatedText,
                isTranslating = state.isTranslating,
                showTranslation = state.showTranslation,
                currentModel = state.currentModel,
                availableModels = state.availableModels,
                onSelectModel = onSelectModel,
                onRecordToggle = checkAndRecord,
                onTranslationToggle = onTranslationToggle,
                onOpenHistory = onOpenHistory,
                onOpenSettings = onOpenSettings,
                modifier = Modifier.fillMaxSize().then(textMargins)
            )
        }

        // ===== 浮层：状态提示（绿色） =====
        state.statusMessage?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(msg, modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }

        // ===== 浮层：错误提示 =====
        state.error?.let { err ->
            Card(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(err, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        // ===== 浮层：翻译模型下载进度 =====
        if (state.isDownloadingModel) {
            Card(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(top = 56.dp, start = 8.dp, end = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.translation_downloading), style = MaterialTheme.typography.bodySmall)
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
            }
        }

        // ===== 元素层：遍历主题中除 TEXT_DISPLAY 外的所有元素 =====
        theme?.elements?.filter { it.type != ElementType.TEXT_DISPLAY }?.forEach { el ->
            val alignment = ThemeEngine.anchorToAlignment(el.position.anchor)
            val margins = ThemeEngine.marginModifier(el.position)

            Box(
                modifier = Modifier.align(alignment).then(margins)
            ) {
                ThemeEngine.RenderElement(
                    config = el,
                    isRecording = state.isRecording,
                    voskReady = state.voskReady,
                    currentText = state.currentText,
                    finalText = state.finalText,
                    translatedText = state.translatedText,
                    isTranslating = state.isTranslating,
                    showTranslation = state.showTranslation,
                    onRecordToggle = checkAndRecord,
                    onTranslationToggle = onTranslationToggle,
                    onOpenHistory = onOpenHistory,
                    onOpenSettings = onOpenSettings,
                    currentModel = state.currentModel,
                    availableModels = state.availableModels,
                    onSelectModel = onSelectModel
                )
            }
        }

        // ===== 底部版权 =====
        Text(
            text = "POWERED BY APIRL",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
        )
    }
}

// ==================== 历史弹窗 ====================

@Composable
private fun HistoryDialog(history: List<HistoryItem>, onDismiss: () -> Unit, onClear: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.history))
                if (history.isNotEmpty()) IconButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clear_all))
                }
            }
        },
        text = {
            if (history.isEmpty()) Text(stringResource(R.string.no_history))
            else Column(Modifier.heightIn(max = 400.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                history.forEach { item ->
                    Card(colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(Modifier.padding(8.dp)) {
                            Text(item.chineseText, style = MaterialTheme.typography.bodyMedium)
                            if (!item.englishText.isNullOrBlank())
                                Text(item.englishText, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary)
                            Text(formatTimestamp(item.timestamp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } }
    )
}

private fun formatTimestamp(millis: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
