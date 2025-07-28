package com.agog.mathdisplay.render

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import com.agog.mathdisplay.graphics.createImageBitmapFromFreetypeBitmap
import com.agog.mathdisplay.parse.MathDisplayException
import com.pvporbit.freetype.FreeTypeConstants


class MTDrawFreeType(val mathfont: MTFontMathTable) {

    fun drawGlyph(canvas: Canvas, p: Paint, gid: Int, x: Float, y: Float) {
        val face = mathfont.checkFontSize()

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
                    val bitmap = createImageBitmapFromFreetypeBitmap(
                        plainbitmap.width,
                        plainbitmap.rows,
                        plainbitmap.buffer
                    )
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