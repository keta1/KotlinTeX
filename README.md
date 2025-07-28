# Katex

[English Version](README-en.md) | [中文版本](README.md)

[![许可证](https://img.shields.io/badge/License-BSD%202--Clause-orange.svg)](https://opensource.org/licenses/BSD-2-Clause)
[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-blue.svg?logo=kotlin)]([http://kotlinlang.org](https://www.jetbrains.com/kotlin-multiplatform/))

一个基于 Kotlin Multiplatform 的跨平台 LaTeX 数学表达式渲染库，支持 Android 和 iOS 平台。

## 关于项目

本项目基于开源项目 [**AndroidMath**](https://github.com/gregcockroft/AndroidMath)
改写而来，将所有代码转换为 Kotlin 并使用 Kotlin Multiplatform 技术实现跨平台支持。通过 Compose
Multiplatform 为 Android 和 iOS 平台提供高质量的 LaTeX 数学表达式渲染功能。

## 特性

- 🚀 基于 Kotlin Multiplatform 技术，Android通过JNI、iOS通过C语言互操作与 FreeType 库集成
- 📱 支持 Android 和 iOS 平台
- 🎨 使用 Compose Multiplatform 进行 UI 渲染
- 📊 完整的 LaTeX 数学表达式支持
- 🔧 易于集成和使用

## 平台支持

- ✅ Android (API 23+)
- ✅ iOS (iOS 13+)

## 依赖

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("io.github.mrl:katex:0.1.0")
}
```

### 项目配置

请确保您的项目已正确配置 Kotlin Multiplatform 和 Compose Multiplatform。

## 使用方法

1. 直接使用Latex字符串

```kotlin
MTMathView(
    latex = "\\frac{a}{b}",
    modifier = Modifier.fillMaxWidth(),
    fontSize = KDefaultFontSize.sp,
    textColor = Color.Black,
    font = null,
    mode = MTMathViewMode.KMTMathViewModeDisplay,
    textAlignment = MTTextAlignment.KMTTextAlignmentLeft,
    displayErrorInline = true,
    errorFontSize = 20.sp,
    minHeight = fontSize * 1.5f,
)
```

2. 在后台线程如 ViewModel 中解析Latex得到

```kotlin
val latexFormulas = listOf(
    "\\frac{a}{b}",
    "\\sqrt{x^2 + y^2}",
    "\\int_0^1 x^2 dx",
    "\\sum_{i=1}^n i^2"
)

@KoinViewModel
class LatexViewModel : ViewModel() {
    val state = MutableStateFlow(emptyList<MTMathList>())

    fun parseLatex() {
        viewModelScope.launch(Dispatchers.Default) {
            state.value = latexFormulas.mapNotNull { latex ->
                if (latex.isNotEmpty()) {
                    val newParseError = MTParseError()
                    val list = MTMathListBuilder.buildFromString(latex, newParseError)
                    if (newParseError.errorCode != MTParseErrors.ErrorNone) {
                        null
                    } else {
                        list
                    }
                } else {
                    null
                }
            }
        }
    }
}
```

```kotlin
@Composable
fun LatexList(
    viewModel: LatexViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        viewModel.parseLatex()
    }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LazyColumn(modifier = modifier) {
        items(state) { mathList ->
            MTMathView(
                mathList = mathList,
                fontSize = KDefaultFontSize.sp,
                textColor = Color.Black,
                font = null,
                mode = MTMathViewMode.KMTMathViewModeDisplay,
                textAlignment = MTTextAlignment.KMTTextAlignmentLeft,
                minHeight = KDefaultFontSize.sp * 1.5f
            )
        }
    }
}
```

## 自行构建

```shell script
# 构建iOS产物
# 下载freetype库到external目录
git submodule update --init --recursive
cd external/freetype
# 构建 FreeType 库
./build-ios-cmake.sh
```

## 版本历史

- **0.1.0** - 初始版本，支持基本的 LaTeX 数学表达式渲染

## 致谢

本项目基于 [AndroidMath](https://github.com/gregcockroft/AndroidMath) 项目开发，感谢原作者的贡献。

## 贡献

欢迎提交 Issue 和 Pull Request 来帮助改进这个项目。

## 联系方式

如有问题或建议，请通过 GitHub Issues 联系我们。
