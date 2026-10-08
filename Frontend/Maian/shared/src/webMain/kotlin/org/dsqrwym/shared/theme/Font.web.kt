package org.dsqrwym.shared.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/**
 * Web（wasmJs）端字体实际实现。
 *
 * 返回 null 表示不指定字体家族：Web 产物里**没有任何字体文件**，
 * 排版直接使用平台默认字体；缺字由 Compose Multiplatform Web 的按需字体回退处理。
 */
@Composable
actual fun appFontFamilyOrNull(): FontFamily? = null

@Composable
actual fun appSignInFontFamilyOrNull(): FontFamily? = null
