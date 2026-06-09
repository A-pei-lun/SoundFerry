package com.example.soundferry.ui.theme

import android.content.Context
import android.util.Log
import java.io.File

/**
 * 主题仓库
 * 负责从 assets（内置）和 filesDir（用户自定义）加载主题
 */
class ThemeRepository(private val context: Context) {

    private val customThemeDir: File
        get() = File(context.filesDir, "custom").also { it.mkdirs() }

    /**
     * 加载所有可用主题
     * 来源：assets/themes/ + filesDir/custom/
     */
    fun loadAllThemes(): List<Theme> {
        val themes = mutableListOf<Theme>()

        // 1. 内置主题（assets）
        try {
            val assetFiles = context.assets.list("themes") ?: emptyArray()
            Log.d(TAG, "assets/themes/ 找到 ${assetFiles.size} 个文件: ${assetFiles.joinToString()}")
            for (fileName in assetFiles) {
                if (!fileName.endsWith(".json")) continue
                try {
                    val json = context.assets.open("themes/$fileName")
                        .bufferedReader().use { it.readText() }
                    val theme = Theme().parseJson(json, fileName)
                    if (theme != null) {
                        themes.add(theme.copy(isBuiltin = true))
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "内置主题解析失败: $fileName", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取 assets/themes 失败", e)
        }

        // 2. 自定义主题（filesDir/custom/ 下的 .json）
        val customDir = customThemeDir
        val customFiles = customDir.listFiles { f -> f.name.endsWith(".json") } ?: emptyArray()
        for (file in customFiles) {
            try {
                val json = file.readText()
                val theme = Theme().parseJson(json, file.name)
                if (theme != null) {
                    themes.add(theme.copy(isBuiltin = false))
                }
            } catch (e: Exception) {
                Log.w(TAG, "自定义主题解析失败: ${file.name}", e)
            }
        }

        return themes
    }

    /**
     * 按文件名查找主题
     */
    fun findTheme(fileName: String): Theme? {
        return loadAllThemes().find { it.fileName == fileName }
    }

    /**
     * 获取自定义资源目录路径（用于设置中打开）
     */
    fun getCustomDir(): File = customThemeDir

    /**
     * 获取自定义资源目录下的元素图片列表
     */
    fun getCustomElementImages(): List<String> {
        val dir = File(customThemeDir, "elements").also { it.mkdirs() }
        return dir.listFiles { f ->
            f.name.endsWith(".png") || f.name.endsWith(".jpg") || f.name.endsWith(".webp")
        }?.map { it.name }?.sorted() ?: emptyList()
    }

    companion object {
        private const val TAG = "ThemeRepository"
    }
}
