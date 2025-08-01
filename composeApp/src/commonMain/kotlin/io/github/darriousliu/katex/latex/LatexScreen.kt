package io.github.darriousliu.katex.latex

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agog.mathdisplay.utils.MTFontManager
import io.github.darriousliu.katex.core.MTMathView
import org.koin.compose.viewmodel.koinViewModel

enum class LatexType {
    MATH_ITEMS, // 用于显示 MathItem 列表
    MATH_LIST,  // 用于显示 MathList 列表
    LATEX,
}

val latexType = LatexType.MATH_ITEMS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LatexScreen(
    modifier: Modifier = Modifier,
    viewModel: LatexViewModel = koinViewModel<LatexViewModel>()
) {
    val density = LocalDensity.current
    var fontSize by remember { mutableStateOf(20.sp) }
    val fontSizePx = with(density) { fontSize.toPx() }

    var mtFont by remember {
        mutableStateOf(MTFontManager.defaultFont())
    }
    var currentFont by remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("CommonMark Kotlin") },
                actions = {
                    // 改变字体
                    MenuAction(
                        onClick = {
                            currentFont = it ?: 0
                            mtFont = when (it) {
                                0 -> MTFontManager.latinModernFontWithSize(fontSizePx)
                                1 -> MTFontManager.termesFontWithSize(fontSizePx)
                                2 -> MTFontManager.xitsFontWithSize(fontSizePx)
                                else -> mtFont
                            }
                        },
                        text = "Font",
                        current = currentFont,
                        menus = listOf(
                            "Latin Modern Math" to 0,
                            "Tex Gyre Terms" to 1,
                            "XITS Math" to 2,
                        )
                    )
                    // 改变字体大小
                    MenuAction(
                        onClick = {
                            fontSize = it ?: 20.sp
                        },
                        text = "Font Size",
                        current = fontSize,
                        menus = listOf(
                            "20sp" to 20.sp,
                            "24sp" to 24.sp,
                            "28sp" to 28.sp,
                            "32sp" to 32.sp,
                            "36sp" to 36.sp,
                            "40sp" to 40.sp
                        )
                    )

                }
            )
        },
    ) {
        LaunchedEffect(Unit) {
            when (latexType) {
                LatexType.MATH_ITEMS -> {
                    viewModel.parseMathItems()
                }

                LatexType.MATH_LIST -> {
                    viewModel.parseMathList()
                }

                LatexType.LATEX -> {
                    viewModel.parseLatexList()
                }
            }
        }

        val mathList by viewModel.mathList.collectAsStateWithLifecycle()
        val mathItems by viewModel.mathItems.collectAsStateWithLifecycle()
        val latexList by viewModel.latexList.collectAsStateWithLifecycle()
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(40.dp),
        ) {
            when (latexType) {
                LatexType.MATH_ITEMS -> {
                    items(
                        items = mathItems,
                        key = { it.latex }
                    ) {
                        MTMathView(
                            mathItem = it,
                        )
                    }
                }

                LatexType.MATH_LIST -> {
                    items(
                        items = mathList,
                        key = { it }
                    ) { math ->
                        MTMathView(
                            mathList = math,
                            fontSize = fontSize,
                            font = mtFont,
                        )
                    }
                }

                LatexType.LATEX -> {
                    items(
                        items = latexList,
                        key = { it }
                    ) {
                        MTMathView(
                            latex = it,
                            fontSize = fontSize,
                            font = mtFont,
                        )
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun <T> MenuAction(
    onClick: (T?) -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    current: T? = null,
    menus: List<Pair<String, T>>? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    Button(
        onClick = {
            if (menus == null) {
                onClick(null)
            } else {
                expanded = true
            }
        },
    ) {
        Text(text)
    }
    if (menus != null) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = modifier
        ) {
            menus.forEach { menu ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(menu.first)
                            Checkbox(
                                checked = current == menu.second,
                                onCheckedChange = null, // 复选框不需要交互
                            )
                        }
                    },
                    onClick = {
                        onClick(menu.second)
                        expanded = false
                    }
                )
            }
        }
    }
}
