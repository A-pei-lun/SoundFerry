package com.example.soundferry.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.soundferry.audio.AudioRecorder
import com.example.soundferry.audio.engine.EngineManager
import com.example.soundferry.data.HistoryRepository
import com.example.soundferry.data.ModelImporter
import com.example.soundferry.data.SettingsRepository
import com.example.soundferry.translate.MLKitTranslator
import com.example.soundferry.translate.Translator
import com.example.soundferry.ui.theme.Anchor
import com.example.soundferry.ui.theme.ElementConfig
import com.example.soundferry.ui.theme.ElementPosition
import com.example.soundferry.ui.theme.ElementStyle
import com.example.soundferry.ui.theme.ElementType
import com.example.soundferry.ui.theme.Theme
import com.example.soundferry.ui.theme.ThemeRepository
import com.example.soundferry.util.TextFormatter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val isRecording: Boolean = false,
    val voskReady: Boolean = false,
    val currentEngine: String = "Vosk",
    val availableEngines: List<String> = listOf("Vosk"),
    val currentModel: String = "vosk-model-small-cn-0.22",
    val availableModels: List<String> = listOf("vosk-model-small-cn-0.22"),
    val isSwitchingModel: Boolean = false,
    val pendingModels: List<String> = emptyList(),
    val statusMessage: String? = null,
    val appLanguage: String = "zh",
    val translatorReady: Boolean = false,
    val currentText: String = "",
    val finalText: String = "",
    val translatedText: String = "",
    val isTranslating: Boolean = false,
    val showTranslation: Boolean = false,
    val downloadProgress: Float = 0f,
    val isDownloadingModel: Boolean = false,
    val error: String? = null,
    // ===== 主题相关 =====
    val themes: List<Theme> = emptyList(),
    val currentThemeFileName: String = "极简黑.json",
    val liquidGlassEnabled: Boolean = false,
    val showSettings: Boolean = false
)

class MainViewModel(
    private val audioRecorder: AudioRecorder,
    private val engineManager: EngineManager,
    private val translator: Translator,
    private val historyRepository: HistoryRepository,
    private val themeRepository: ThemeRepository,
    private val settingsRepository: SettingsRepository,
    private val modelImporter: ModelImporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    val history = historyRepository.allHistory

    init {
        scanEngines()
        scanModels()
        scanPendingModels()
        initEngine()
        viewModelScope.launch { historyRepository.load() }
        loadThemes()
        loadSettings()
    }

    /** 从 DataStore 加载已保存的设置 */
    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.currentThemeFileName.collect { themeName ->
                _uiState.update { it.copy(currentThemeFileName = themeName) }
            }
        }
        viewModelScope.launch {
            settingsRepository.liquidGlassEnabled.collect { enabled ->
                _uiState.update { it.copy(liquidGlassEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            settingsRepository.appLanguage.collect { lang ->
                _uiState.update { it.copy(appLanguage = lang) }
            }
        }
    }

    /** 切换语言（重启后生效） */
    fun switchLanguage(lang: String) {
        if (lang == _uiState.value.appLanguage) return
        viewModelScope.launch {
            settingsRepository.saveLanguage(lang)
            _uiState.update { it.copy(appLanguage = lang) }
        }
        _uiState.update { it.copy(statusMessage = "语言设置已保存，重启 APP 后生效") }
        viewModelScope.launch {
            delay(3000)
            _uiState.update { it.copy(statusMessage = null) }
        }
    }

    // ===== 引擎 =====

    private fun scanEngines() {
        val engines = engineManager.getAvailableEngines()
        _uiState.update { it.copy(availableEngines = engines, currentEngine = engines.firstOrNull() ?: "Vosk") }
    }

    /** 切换 ASR 引擎 */
    fun switchEngine(engineName: String) {
        if (_uiState.value.isRecording) {
            _uiState.update { it.copy(error = "请先停止录音再切换引擎") }
            return
        }
        if (engineManager.switchEngine(engineName)) {
            _uiState.update { it.copy(currentEngine = engineName, voskReady = false, availableModels = emptyList()) }
            scanModels()
            initEngine()
        }
    }

    // ===== 模型 =====

    private fun scanModels() {
        val models = engineManager.getAvailableModels()
        _uiState.update { it.copy(availableModels = models) }
    }

    fun switchModel(modelFolder: String) {
        if (_uiState.value.isRecording) {
            _uiState.update { it.copy(error = "请先停止录音再切换模型") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSwitchingModel = true) }
            val success = engineManager.switchModel(modelFolder)
            if (success) {
                _uiState.update { it.copy(currentModel = modelFolder, isSwitchingModel = false, voskReady = true) }
            } else {
                _uiState.update { it.copy(error = "模型切换失败", isSwitchingModel = false) }
            }
        }
    }

    // ===== 模型导入 =====

    private fun scanPendingModels() {
        val pending = modelImporter.scanPendingZips()
        _uiState.update { it.copy(pendingModels = pending) }
    }

    /** 导入模型（解压 zip） */
    fun importModel(zipFileName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = null) }
            val result = modelImporter.importModel(zipFileName)
            if (result != null) {
                // 刷新模型列表
                scanModels()
                scanPendingModels()
                _uiState.update { it.copy(error = "模型「$result」导入成功，可在模型选择中切换") }
            } else {
                _uiState.update { it.copy(error = "模型导入失败：$zipFileName") }
            }
        }
    }

    /** 导入后删除压缩包 */
    fun deletePendingZip(zipFileName: String) {
        modelImporter.deleteZip(zipFileName)
        scanPendingModels()
    }

    // ===== 主题 =====

    private fun loadThemes() {
        var themes = themeRepository.loadAllThemes()

        // 如果 JSON 没加载到，用硬编码兜底主题
        if (themes.isEmpty()) {
            themes = listOf(
                // 主题1：极简黑 — 按钮底部居中，功能键顶部
                Theme(
                    name = "极简黑（默认）",
                    fileName = "极简黑.json",
                    elements = listOf(
                        ElementConfig(ElementType.RECORD_BUTTON,
                            position = ElementPosition(Anchor.BOTTOM_CENTER, marginBottom = 40),
                            style = ElementStyle(size = 80, shape = "circle", colorActive = "#D32F2F", colorIdle = "#1976D2")),
                        ElementConfig(ElementType.TEXT_DISPLAY,
                            position = ElementPosition(marginTop = 48),
                            style = ElementStyle(textSize = 18, cornerRadius = 16, bgColor = "#FFF0F0F0")),
                        ElementConfig(ElementType.HISTORY_BUTTON,
                            position = ElementPosition(Anchor.TOP_LEFT, marginLeft = 8, marginTop = 8),
                            style = ElementStyle(iconSize = 24)),
                        ElementConfig(ElementType.MODEL_SELECTOR,
                            position = ElementPosition(Anchor.TOP_RIGHT, marginTop = 8, marginRight = 8)),
                        ElementConfig(ElementType.SETTINGS_BUTTON,
                            position = ElementPosition(Anchor.TOP_RIGHT, marginTop = 8, marginRight = 48),
                            style = ElementStyle(iconSize = 24)),
                        ElementConfig(ElementType.TRANSLATION_TOGGLE,
                            position = ElementPosition(Anchor.BOTTOM_LEFT, marginLeft = 16, marginBottom = 104),
                            style = ElementStyle(colorActive = "#1976D2"))
                    )
                ),
                // 主题2：琉璃 — 布局同极简黑，颜色/形状不同
                Theme(
                    name = "琉璃",
                    fileName = "琉璃.json",
                    elements = listOf(
                        ElementConfig(ElementType.RECORD_BUTTON,
                            position = ElementPosition(Anchor.BOTTOM_CENTER, marginBottom = 40),
                            style = ElementStyle(size = 80, shape = "circle", colorActive = "#FF1744", colorIdle = "#00E676")),
                        ElementConfig(ElementType.TEXT_DISPLAY,
                            position = ElementPosition(marginTop = 48),
                            style = ElementStyle(textSize = 18, cornerRadius = 16, bgColor = "#CCE0F7FA")),
                        ElementConfig(ElementType.HISTORY_BUTTON,
                            position = ElementPosition(Anchor.TOP_LEFT, marginLeft = 8, marginTop = 8),
                            style = ElementStyle(iconSize = 24)),
                        ElementConfig(ElementType.MODEL_SELECTOR,
                            position = ElementPosition(Anchor.TOP_RIGHT, marginTop = 8, marginRight = 8)),
                        ElementConfig(ElementType.SETTINGS_BUTTON,
                            position = ElementPosition(Anchor.TOP_RIGHT, marginTop = 8, marginRight = 48),
                            style = ElementStyle(iconSize = 24)),
                        ElementConfig(ElementType.TRANSLATION_TOGGLE,
                            position = ElementPosition(Anchor.BOTTOM_LEFT, marginLeft = 16, marginBottom = 104),
                            style = ElementStyle(colorActive = "#00E5FF"))
                    )
                )
            )
        }

        _uiState.update { it.copy(themes = themes) }
    }

    fun selectTheme(fileName: String) {
        _uiState.update { it.copy(currentThemeFileName = fileName) }
        viewModelScope.launch { settingsRepository.saveTheme(fileName) }
    }

    fun toggleLiquidGlass() {
        val newState = !_uiState.value.liquidGlassEnabled
        _uiState.update { it.copy(liquidGlassEnabled = newState) }
        viewModelScope.launch { settingsRepository.saveLiquidGlass(newState) }
    }

    // ===== 核心功能 =====

    private fun initEngine() {
        viewModelScope.launch {
            try {
                engineManager.init(_uiState.value.currentModel)
                _uiState.update { it.copy(voskReady = true) }
                Log.d(TAG, "Vosk init ok")
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "语音模型加载失败：${e.message}") }
            }
        }
    }

    fun downloadTranslationModel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDownloadingModel = true, downloadProgress = 0f) }
            val success = (translator as? MLKitTranslator)?.downloadModel()?.collect { result ->
                if (result) {
                    _uiState.update { it.copy(translatorReady = true, isDownloadingModel = false, statusMessage = "翻译模型已就绪") }
                } else {
                    _uiState.update { it.copy(isDownloadingModel = false, error = "翻译模型下载失败，请检查网络") }
                }
            }
            delay(3000)
            _uiState.update { it.copy(statusMessage = null) }
        }
    }

    fun toggleRecording() {
        if (_uiState.value.isRecording) stopRecording() else startRecording()
    }

    private fun startRecording() {
        if (!_uiState.value.voskReady) {
            _uiState.update { it.copy(error = "语音模型还没加载完") }
            return
        }
        _uiState.update {
            it.copy(isRecording = true, currentText = "", finalText = "",
                translatedText = "", error = null)
        }

        viewModelScope.launch {
            audioRecorder.start(
                onPartial = { partial ->
                    _uiState.update { it.copy(currentText = TextFormatter.formatText(partial)) }
                }
            )
        }
    }

    private fun stopRecording() {
        val rawText = audioRecorder.stop()
        val finalText = TextFormatter.formatText(rawText)
        _uiState.update { it.copy(isRecording = false, finalText = finalText) }

        if (_uiState.value.showTranslation && _uiState.value.translatorReady && finalText.isNotBlank()) {
            translateResult(finalText)
        }

        if (finalText.isNotBlank()) {
            viewModelScope.launch {
                val english = if (_uiState.value.showTranslation)
                    _uiState.value.translatedText else null
                historyRepository.insert(finalText, english)
            }
        }
    }

    private fun translateResult(text: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true) }
            val translated = translator.translate(text)
            val formatted = TextFormatter.formatText(translated)
            _uiState.update { it.copy(translatedText = formatted, isTranslating = false) }
        }
    }

    fun toggleTranslation() {
        _uiState.update { it.copy(statusMessage = "翻译功能开发中，敬请期待") }
        viewModelScope.launch {
            delay(2000)
            _uiState.update { it.copy(statusMessage = null) }
        }
    }

    fun showError(msg: String) {
        _uiState.update { it.copy(error = msg) }
    }

    fun openSettings() {
        _uiState.update { it.copy(showSettings = true) }
    }

    fun closeSettings() {
        _uiState.update { it.copy(showSettings = false) }
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.deleteAll() }
    }

    override fun onCleared() {
        super.onCleared()
        audioRecorder.stop()
        engineManager.release()
        (translator as? MLKitTranslator)?.release()
    }

    companion object {
        private const val TAG = "MainViewModel"
    }
}
