package io.github.darriousliu.katex.core

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.*
import com.agog.mathdisplay.parse.*
import com.agog.mathdisplay.render.MTFont
import com.agog.mathdisplay.render.MTMathListDisplay
import com.agog.mathdisplay.render.MTTypesetter
import com.agog.mathdisplay.utils.KDefaultFontSize
import com.agog.mathdisplay.utils.MTFontManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 共享数据类，用于管理 MTMathView 的状态
 */
@Stable
private data class MTMathViewState(
    val displayList: MTMathListDisplay? = null,
    val currentFont: MTFont? = null,
    val calculatedWidth: Dp = 0.dp,
    val calculatedHeight: Dp = 0.dp,
    val parseError: MTParseError? = null,
    val isReady: Boolean = false
)

/**
 * 核心的 MTMathView 实现，包含所有共同逻辑
 */
@Composable
private fun MTMathViewCore(
    mathListProvider: () -> MTMathList?,
    parseErrorProvider: () -> MTParseError?,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = KDefaultFontSize.sp,
    textColor: Color = Color.Black,
    font: MTFont? = null,
    mode: MTMathViewMode = MTMathViewMode.KMTMathViewModeDisplay,
    textAlignment: MTTextAlignment = MTTextAlignment.KMTTextAlignmentLeft,
    displayErrorInline: Boolean = false,
    errorFontSize: TextUnit = 20.sp,
    minHeight: TextUnit = fontSize * 1.5f,
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    // 获取当前数据
    val currentMathList = mathListProvider()
    val currentParseError = parseErrorProvider()
    // 状态管理
    var state by remember { mutableStateOf(MTMathViewState()) }

    // 预估初始高度，避免在LazyColumn中高度跳变
    val estimatedHeight = remember(fontSize) {
        with(density) { minHeight.toDp() }
    }

    // 合并所有状态更新到单个 LaunchedEffect 中
    LaunchedEffect(fontSize, font, currentMathList, mode, currentParseError, displayErrorInline) {
        with(density) {
            // 1. 更新字体
            val newFont = (font ?: MTFontManager.defaultFont()).copyFontWithSize(fontSize.toPx())

            // 2. 更新显示列表
            val newDisplayList = if (currentMathList != null) {
                val style = when (mode) {
                    MTMathViewMode.KMTMathViewModeDisplay -> MTLineStyle.KMTLineStyleDisplay
                    MTMathViewMode.KMTMathViewModeText -> MTLineStyle.KMTLineStyleText
                }
                MTTypesetter.createLineForMathList(currentMathList, newFont, style)
            } else {
                null
            }

            // 3. 计算尺寸
            val (width, height) = when {
                newDisplayList != null -> {
                    val width = (newDisplayList.width + 1).toDp()
                    val height = (newDisplayList.ascent + newDisplayList.descent + 1).toDp()
                    Pair(width, height)
                }

                currentParseError != null && currentParseError.errorCode != MTParseErrors.ErrorNone && displayErrorInline -> {
                    // 计算错误文本尺寸
                    val errorTextSizeDp = errorFontSize.toDp()
                    val estimatedWidth = currentParseError.errorDesc.length * errorTextSizeDp * 0.6f
                    Pair(estimatedWidth, errorTextSizeDp * 1.2f)
                }

                else -> Pair(0.dp, 0.dp)
            }

            // 4. 一次性更新所有状态，避免中间状态
            state = MTMathViewState(
                displayList = newDisplayList,
                currentFont = newFont,
                calculatedWidth = width,
                calculatedHeight = height,
                parseError = currentParseError,
                isReady = true
            )
        }
    }

    Box(
        modifier = modifier.size(
            width = if (state.isReady) state.calculatedWidth else 1.dp,
            height = if (state.isReady) state.calculatedHeight else estimatedHeight
        )
    ) {
        // 渲染
        if (state.isReady) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val parseError = state.parseError
                val displayList = state.displayList

                when {
                    parseError != null && parseError.errorCode != MTParseErrors.ErrorNone && displayErrorInline -> {
                        drawError(parseError.errorDesc, errorFontSize, Color.Red, textMeasurer)
                    }

                    displayList != null -> {
                        drawMathFormula(displayList, textColor, textAlignment)
                    }
                }
            }
        }
    }
}

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
    errorFontSize: TextUnit = 20.sp,
    minHeight: TextUnit = fontSize * 1.5f,
) {
    // 解析状态
    var parseError by remember { mutableStateOf(MTParseError()) }
    var mathList by remember { mutableStateOf<MTMathList?>(null) }

    // 解析 LaTeX
    LaunchedEffect(latex) {
        withContext(Dispatchers.Default) {
            if (latex.isNotEmpty()) {
                val newParseError = MTParseError()
                val list = MTMathListBuilder.buildFromString(latex, newParseError)
                parseError = newParseError
                mathList = if (newParseError.errorCode != MTParseErrors.ErrorNone) {
                    null
                } else {
                    list
                }
            } else {
                mathList = null
                parseError = MTParseError()
            }
        }
    }

    MTMathViewCore(
        mathListProvider = { mathList },
        parseErrorProvider = { parseError },
        modifier = modifier,
        fontSize = fontSize,
        textColor = textColor,
        font = font,
        mode = mode,
        textAlignment = textAlignment,
        displayErrorInline = displayErrorInline,
        errorFontSize = errorFontSize,
        minHeight = minHeight
    )
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
    textAlignment: MTTextAlignment = MTTextAlignment.KMTTextAlignmentLeft,
    minHeight: TextUnit = fontSize * 1.5f,
) {
    MTMathViewCore(
        mathListProvider = { mathList },
        parseErrorProvider = { null }, // 直接使用 MTMathList 时没有解析错误
        modifier = modifier,
        fontSize = fontSize,
        textColor = textColor,
        font = font,
        mode = mode,
        textAlignment = textAlignment,
        displayErrorInline = false, // 不显示错误，因为没有解析过程
        errorFontSize = 20.sp,
        minHeight = minHeight
    )
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
    if (eqHeight < size.height / 2) {
        eqHeight = size.height / 2
    }
    val textY = (size.height - eqHeight) / 2 + displayList.descent

    // 设置位置
    displayList.position.x = textX
    displayList.position.y = textY

    // 绘制（翻转 Y 轴以匹配数学坐标系）
    scale(1f, -1f) {
        displayList.draw(drawContext.canvas)
    }
}

/**
 * 绘制错误信息
 */
private fun DrawScope.drawError(
    errorMessage: String,
    errorFontSize: TextUnit,
    errorColor: Color,
    textMeasurer: TextMeasurer
) {
    drawText(
        textMeasurer = textMeasurer,
        text = errorMessage,
        style = TextStyle(
            color = errorColor,
            fontSize = errorFontSize
        ),
    )
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