package com.agog.mathdisplay.graphics

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.pvporbit.freetype.NativeBinaryBuffer

/**
 * 从 FreeType 位图创建 Compose ImageBitmap
 */
internal expect fun createImageBitmapFromFreetypeBitmap(
    width: Int,
    height: Int,
    buffer: NativeBinaryBuffer
): ImageBitmap

internal expect fun createPlatformPaint(): Paint