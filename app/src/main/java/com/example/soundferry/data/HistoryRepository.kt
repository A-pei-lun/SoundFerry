package com.example.soundferry.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 历史记录数据
 */
data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val chineseText: String,
    val englishText: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * JSON 文件存储的历史记录仓库
 * 零依赖，使用 Android 内置 org.json
 */
class HistoryRepository(private val context: Context) {

    private val file = File(context.filesDir, "history.json")

    private val _allHistory = MutableStateFlow<List<HistoryItem>>(emptyList())
    val allHistory: Flow<List<HistoryItem>> = _allHistory.asStateFlow()

    /** 从文件加载历史 */
    suspend fun load() {
        withContext(Dispatchers.IO) {
            if (!file.exists()) {
                _allHistory.value = emptyList()
                return@withContext
            }
            try {
                val json = file.readText()
                val array = JSONArray(json)
                val list = mutableListOf<HistoryItem>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        HistoryItem(
                            id = obj.getLong("id"),
                            chineseText = obj.getString("chineseText"),
                            englishText = obj.optString("englishText", null),
                            timestamp = obj.getLong("timestamp")
                        )
                    )
                }
                _allHistory.value = list.sortedByDescending { it.timestamp }
            } catch (_: Exception) {
                _allHistory.value = emptyList()
            }
        }
    }

    /** 插入一条记录 */
    suspend fun insert(chineseText: String, englishText: String?) {
        withContext(Dispatchers.IO) {
            val item = HistoryItem(chineseText = chineseText, englishText = englishText)
            val current = _allHistory.value.toMutableList()
            current.add(0, item)
            saveToFile(current)
            _allHistory.value = current
        }
    }

    /** 清空所有记录 */
    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            file.delete()
            _allHistory.value = emptyList()
        }
    }

    private fun saveToFile(list: List<HistoryItem>) {
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("chineseText", item.chineseText)
                put("englishText", item.englishText ?: JSONObject.NULL)
                put("timestamp", item.timestamp)
            }
            array.put(obj)
        }
        file.writeText(array.toString(2))
    }
}
