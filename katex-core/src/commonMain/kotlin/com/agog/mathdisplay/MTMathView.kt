package com.agog.mathdisplay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import com.agog.mathdisplay.parse.*
import com.agog.mathdisplay.render.MTFont
import com.agog.mathdisplay.render.MTMathListDisplay
import com.agog.mathdisplay.render.MTTypesetter
import com.agog.mathdisplay.utils.KDefaultFontSize
import com.agog.mathdisplay.utils.MTFontManager

/**
 * Compose 版本的数学公式显示组件
 *
 * 支持 LaTeX 格式的数学公式渲染
 *
 * @param latex LaTeX 格式的数学公式字符串
 * @param modifier Modifier
 * @param fontSize 字体大小（以 sp 为单位）
 * @param textColor 文本颜色
 * @param font 字体，默认使用 MTFontManager.defaultFont()
 * @param mode 显示模式：Display 模式或 Text 模式
 * @param textAlignment 文本对齐方式
 * @param displayErrorInline 是否内联显示解析错误
 * @param errorFontSize 错误文本的字体大小
 */
@Composable
fun MTMathView(
    latex: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = KDefaultFontSize.sp,
    textColor: Color = Color.Black,
    font: MTFont? = null,
    mode: MTMathViewMode = MTMathViewMode.KMTMathViewModeDisplay,
    textAlignment: MTTextAlignment = MTTextAlignment.KMTTextAlignmentLeft,
    displayErrorInline: Boolean = true,
    errorFontSize: TextUnit = 20.sp
) {
    val density = LocalDensity.current

    // 解析状态
    var parseError by remember { mutableStateOf(MTParseError()) }
    var mathList by remember { mutableStateOf<MTMathList?>(null) }
    var displayList by remember { mutableStateOf<MTMathListDisplay?>(null) }

    // 字体状态
    var currentFont by remember {
        with(density) {
            mutableStateOf(font ?: MTFontManager.defaultFont().copyFontWithSize(fontSize.toPx()))
        }
    }

    // 解析 LaTeX
    LaunchedEffect(latex) {
        if (latex.isNotEmpty()) {
            val list = MTMathListBuilder.buildFromString(latex, parseError)
            mathList = if (parseError.errorCode != MTParseErrors.ErrorNone) {
                null
            } else {
                list
            }
            displayList = null
        }
    }

    // 更新字体
    LaunchedEffect(fontSize, font) {
        with(density) {
            currentFont = (font ?: MTFontManager.defaultFont()).copyFontWithSize(fontSize.toPx())
            displayList = null
        }
    }

    // 更新显示列表
    LaunchedEffect(mathList, currentFont, mode) {
        val ml = mathList
        val cf = currentFont
        if (ml != null) {
            val style = when (mode) {
                MTMathViewMode.KMTMathViewModeDisplay -> MTLineStyle.KMTLineStyleDisplay
                MTMathViewMode.KMTMathViewModeText -> MTLineStyle.KMTLineStyleText
            }
            displayList = MTTypesetter.createLineForMathList(ml, cf, style)
        }
    }

    // 计算尺寸
    val (calculatedWidth, calculatedHeight) = remember(
        displayList,
        parseError,
        displayErrorInline
    ) {
        val dl = displayList
        if (dl != null) {
            val width = dl.width.dp
            val height = (dl.ascent + dl.descent).dp
            Pair(width, height)
        } else if (parseError.errorCode != MTParseErrors.ErrorNone && displayErrorInline) {
            // 计算错误文本尺寸
            val errorTextSizeDp = with(density) { errorFontSize.toDp() }
            val estimatedWidth = parseError.errorDesc.length * errorTextSizeDp * 0.6f
            Pair(estimatedWidth, errorTextSizeDp * 1.2f)
        } else {
            Pair(0.dp, 0.dp)
        }
    }

    Canvas(
        modifier = modifier.size(
            width = max(calculatedWidth, 1.dp),
            height = max(calculatedHeight, 1.dp)
        )
    ) {
        if (parseError.errorCode != MTParseErrors.ErrorNone && displayErrorInline) {
            drawError(parseError.errorDesc, errorFontSize.toPx(), Color.Red)
        } else {
            val dl = displayList
            if (dl != null) {
                drawMathFormula(dl, textColor, textAlignment)
            }
        }
    }
}

/**
 * 直接使用 MTMathList 的 Compose 组件版本
 */
@Composable
fun MTMathView(
    mathList: MTMathList,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = KDefaultFontSize.sp,
    textColor: Color = Color.Black,
    font: MTFont? = null,
    mode: MTMathViewMode = MTMathViewMode.KMTMathViewModeDisplay,
    textAlignment: MTTextAlignment = MTTextAlignment.KMTTextAlignmentLeft
) {
    val density = LocalDensity.current
    var displayList by remember { mutableStateOf<MTMathListDisplay?>(null) }
    var currentFont by remember {
        with(density) {
            mutableStateOf(font ?: MTFontManager.defaultFont().copyFontWithSize(fontSize.toPx()))
        }
    }

    // 更新字体
    LaunchedEffect(fontSize, font) {
        with(density) {
            currentFont = (font ?: MTFontManager.defaultFont()).copyFontWithSize(fontSize.toPx())
            displayList = null
        }
    }

    // 更新显示列表
    LaunchedEffect(mathList, currentFont, mode) {
        val cf = currentFont
        val style = when (mode) {
            MTMathViewMode.KMTMathViewModeDisplay -> MTLineStyle.KMTLineStyleDisplay
            MTMathViewMode.KMTMathViewModeText -> MTLineStyle.KMTLineStyleText
        }
        displayList = MTTypesetter.createLineForMathList(mathList, cf, style)
    }

    // 计算尺寸
    val (calculatedWidth, calculatedHeight) = remember(displayList) {
        val dl = displayList
        if (dl != null) {
            val width = dl.width.dp
            val height = (dl.ascent + dl.descent).dp
            Pair(width, height)
        } else {
            Pair(0.dp, 0.dp)
        }
    }

    Canvas(
        modifier = modifier.size(
            width = max(calculatedWidth, 1.dp),
            height = max(calculatedHeight, 1.dp)
        )
    ) {
        val dl = displayList
        if (dl != null) {
            drawMathFormula(dl, textColor, textAlignment)
        }
    }
}

/**
 * 绘制数学公式
 */
private fun DrawScope.drawMathFormula(
    displayList: MTMathListDisplay,
    textColor: Color,
    textAlignment: MTTextAlignment
) {
    displayList.textColor = textColor.toArgb()

    // 根据对齐方式计算 X 位置
    val textX = when (textAlignment) {
        MTTextAlignment.KMTTextAlignmentLeft -> 0f
        MTTextAlignment.KMTTextAlignmentCenter ->
            (size.width - displayList.width) / 2

        MTTextAlignment.KMTTextAlignmentRight ->
            size.width - displayList.width
    }

    // 计算 Y 位置（垂直居中）
    var eqHeight = displayList.ascent + displayList.descent
    if (eqHeight < size.height / 4) {
        eqHeight = size.height / 4
    }
    val textY = (size.height - eqHeight) / 2 + displayList.descent

    // 设置位置
    displayList.position.x = textX
    displayList.position.y = textY

    // 绘制（翻转 Y 轴以匹配数学坐标系）
    translate(0f, size.height) {
        scale(1f, -1f) {
            displayList.draw(drawContext.canvas)
        }
    }
}

/**
 * 绘制错误信息
 */
private fun DrawScope.drawError(
    errorMessage: String,
    errorFontSize: Float,
    errorColor: Color
) {
    // 注意：这里需要使用原生 Canvas 来绘制文本
    // 在实际项目中，您可能需要使用 androidx.compose.ui.text.drawText
    // 或者创建一个 Text Composable 来显示错误
}

/**
 * 数学显示模式
 */
enum class MTMathViewMode {
    /// Display 模式，相当于 TeX 中的 $$
    KMTMathViewModeDisplay,

    /// Text 模式，相当于 TeX 中的 $
    KMTMathViewModeText
}

/**
 * 文本对齐方式
 */
enum class MTTextAlignment {
    /// 左对齐
    KMTTextAlignmentLeft,

    /// 居中对齐
    KMTTextAlignmentCenter,

    /// 右对齐
    KMTTextAlignmentRight
}