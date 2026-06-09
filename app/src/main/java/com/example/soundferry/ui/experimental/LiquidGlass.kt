package com.example.soundferry.ui.experimental

import android.app.Activity
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * 液化玻璃效果（实验性）
 * 仅 Android 12+ (API 31) 支持
 *
 * 原理：对 Window 背景设置模糊，UI 元素保持清晰，
 * 搭配半透明元素背景呈现毛玻璃效果。
 */
@Composable
fun LiquidGlassEffect(
    enabled: Boolean,
    blurRadius: Int = 25
) {
    if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val window = activity.window

    DisposableEffect(enabled, blurRadius) {
        try {
            window.setBackgroundBlurRadius(blurRadius)
        } catch (_: Exception) {}

        onDispose {
            try {
                window.setBackgroundBlurRadius(0)
            } catch (_: Exception) {}
        }
    }
}
