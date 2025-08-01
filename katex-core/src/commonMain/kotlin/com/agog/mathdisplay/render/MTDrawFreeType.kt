package com.agog.mathdisplay.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import com.agog.mathdisplay.graphics.createImageBitmapFromFreetypeBitmap
import com.agog.mathdisplay.parse.MathDisplayException
import com.agog.mathdisplay.utils.BitmapCache
import com.pvporbit.freetype.FreeTypeConstants


class MTDrawFreeType(val mathFont: MTFontMathTable) {

    fun drawGlyph(canvas: Canvas, p: Paint, gid: Int, x: Float, y: Float) {
        val face = mathFont.checkFontSize()

        // 先用 NO_RENDER 模式获取字形信息（不加载实际 bitmap）
        if (gid != 0 && !face.loadGlyph(gid, FreeTypeConstants.FT_LOAD_NO_BITMAP)) {
            val gslot = face.glyphSlot
            val metrics = gslot?.metrics

            if (metrics != null) {
                // 根据 metrics 估算实际渲染尺寸
                val estimatedWidth = (metrics.width / 64.0f).toInt()
                val estimatedHeight = (metrics.height / 64.0f).toInt()

                // 构建包含估算尺寸的缓存键
                val cacheKey = "$gid-${mathFont.font.name}-${mathFont.font.fontSize}-${estimatedWidth}x${estimatedHeight}"

                val cachedBitmap = BitmapCache[cacheKey]
                if (cachedBitmap != null) {
                    val offx = metrics.horiBearingX / 64.0f
                    val offy = metrics.horiBearingY / 64.0f
                    canvas.drawImage(cachedBitmap, Offset(x + offx, y - offy), p)
                    return
                }
            }
        }


        /* load glyph image into the slot and render (erase previous one) */
        if (gid != 0 && !face.loadGlyph(gid, FreeTypeConstants.FT_LOAD_RENDER)) {
            val gslot = face.glyphSlot
            val plainbitmap = gslot?.bitmap
            if (plainbitmap != null) {
                if (plainbitmap.width == 0 || plainbitmap.rows == 0) {
                    if (gid != 1 && gid != 33) {
                        throw MathDisplayException("missing glyph slot $gid.")
                    }
                } else {
                    val cacheKey =
                        "$gid-${mathFont.font.name}-${mathFont.font.fontSize}-${plainbitmap.width}x${plainbitmap.rows}"
                    val cachedBitmap = BitmapCache[cacheKey]
                    val bitmap = cachedBitmap ?: createImageBitmapFromFreetypeBitmap(
                        plainbitmap.width,
                        plainbitmap.rows,
                        plainbitmap.buffer
                    ).also {
                        BitmapCache[cacheKey] = it
                    }
                    val metrics = gslot.metrics!!
                    val offx =
                        metrics.horiBearingX / 64.0f  // 26.6 fixed point integer from freetype
                    val offy = metrics.horiBearingY / 64.0f
                    canvas.drawImage(bitmap, Offset(x + offx, y - offy), p)
                }
            }
        }
    }
}