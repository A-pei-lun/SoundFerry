package com.example.soundferry.ui.theme

import org.json.JSONArray
import org.json.JSONObject

/**
 * 锚点位置
 */
enum class Anchor {
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    CENTER_LEFT, CENTER, CENTER_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
}

/**
 * 元素位置配置
 */
data class ElementPosition(
    val anchor: Anchor = Anchor.CENTER,
    val marginLeft: Int = 0,
    val marginTop: Int = 0,
    val marginRight: Int = 0,
    val marginBottom: Int = 0
)

/**
 * 元素样式配置
 * 通用字段 + 各元素专有字段通过 custom 扩展
 */
data class ElementStyle(
    val size: Int? = null,            // 尺寸
    val textSize: Int? = null,        // 字号
    val fontSize: Int? = null,        // 字体大小（用于下拉菜单等）
    val iconSize: Int? = null,        // 图标大小
    val cornerRadius: Int? = null,    // 圆角
    val shape: String? = null,        // 形状 circle / rounded / square
    val color: String? = null,        // 颜色（十六进制 #RRGGBB 或 #AARRGGBB）
    val colorActive: String? = null,  // 激活态颜色
    val colorIdle: String? = null,    // 待机态颜色
    val bgColor: String? = null,      // 背景色
    val bgImage: String? = null,      // 背景图（文件名，从 assets/elements/ 加载）
    val showBorder: Boolean? = null,  // 是否显示边框
    val custom: Map<String, Any>? = null  // 扩展字段
)

/**
 * 元素配置
 */
data class ElementConfig(
    val type: ElementType,
    val position: ElementPosition = ElementPosition(),
    val style: ElementStyle = ElementStyle()
)

/**
 * 主题
 */
data class Theme(
    val name: String = "",               // 主题名（中文，如"极简黑"）
    val fileName: String = "",           // 文件名（用于定位）
    val elements: List<ElementConfig> = emptyList(),
    val isBuiltin: Boolean = true   // true=assets内置, false=用户自定义
)

// ==================== JSON 解析 ====================

fun Theme.parseJson(json: String, fileName: String): Theme? {
    return try {
        val obj = JSONObject(json)
        val name = obj.getString("name")
        val elementsArr = obj.optJSONArray("elements") ?: JSONArray()

        val elements = mutableListOf<ElementConfig>()
        for (i in 0 until elementsArr.length()) {
            val el = elementsArr.getJSONObject(i)
            val typeName = el.getString("type")
            val type = try { ElementType.valueOf(typeName) } catch (e: Exception) { continue }

            val pos = el.optJSONObject("position")
            val styleObj = el.optJSONObject("style")

            elements.add(ElementConfig(
                type = type,
                position = pos?.toPosition() ?: ElementPosition(),
                style = styleObj?.toStyle() ?: ElementStyle()
            ))
        }

        Theme(name = name, fileName = fileName, elements = elements)
    } catch (e: Exception) {
        null
    }
}

private fun JSONObject.toPosition(): ElementPosition {
    return ElementPosition(
        anchor = try { Anchor.valueOf(optString("anchor", "CENTER")) } catch (_: Exception) { Anchor.CENTER },
        marginLeft = optInt("marginLeft", 0),
        marginTop = optInt("marginTop", 0),
        marginRight = optInt("marginRight", 0),
        marginBottom = optInt("marginBottom", 0)
    )
}

private fun JSONObject.toStyle(): ElementStyle {
    return ElementStyle(
        size = optIntOrNull("size"),
        textSize = optIntOrNull("textSize"),
        fontSize = optIntOrNull("fontSize"),
        iconSize = optIntOrNull("iconSize"),
        cornerRadius = optIntOrNull("cornerRadius"),
        shape = optStringOrNull("shape"),
        color = optStringOrNull("color"),
        colorActive = optStringOrNull("colorActive"),
        colorIdle = optStringOrNull("colorIdle"),
        bgColor = optStringOrNull("bgColor"),
        bgImage = optStringOrNull("bgImage"),
        showBorder = if (has("showBorder")) optBoolean("showBorder") else null,
        custom = null
    )
}

private fun JSONObject.optIntOrNull(key: String): Int? =
    if (has(key) && !isNull(key)) optInt(key) else null

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) optString(key) else null
