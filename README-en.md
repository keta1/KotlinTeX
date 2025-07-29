这是将该README文件翻译成英文的版本：

# KotlinTeX

[English Version](README-en.md) | [中文版本](README.md)

[![License](https://img.shields.io/badge/License-BSD%202--Clause-orange.svg)](https://opensource.org/licenses/BSD-2-Clause)
[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-blue.svg?logo=kotlin)]([http://kotlinlang.org](https://www.jetbrains.com/kotlin-multiplatform/))

A cross-platform LaTeX mathematical expression rendering library based on Kotlin Multiplatform,
supporting Android and iOS platforms.

## Display

https://github.com/user-attachments/assets/ee302c81-8f74-440e-97e0-224ff32b4516

## About the Project

This project is rewritten based on the open-source project
[**AndroidMath**](https://github.com/gregcockroft/AndroidMath),
converting all code to Kotlin and implementing cross-platform support using Kotlin Multiplatform
technology. It provides high-quality LaTeX mathematical expression rendering functionality for
Android and iOS platforms through Compose Multiplatform.

## Features

- 🚀 Based on Kotlin Multiplatform technology, integrating with FreeType library through JNI on
  Android and C interop on iOS
- 📱 Supports Android and iOS platforms
- 🎨 UI rendering using Compose Multiplatform
- 📊 Complete LaTeX mathematical expression support
- 🔧 Easy integration and usage

## Platform Support

- ✅ Android (API 23+)
- ✅ iOS (iOS 13+)

## Dependencies

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("io.github.mrl:katex:0.1.0")
}
```

### Project Configuration

Please ensure your project is properly configured with Kotlin Multiplatform and Compose
Multiplatform.

## Usage

1. Direct usage with LaTeX string

```kotlin
@Composable
fun LatexExample() {
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
}

```

2. Parse LaTeX in background thread such as in ViewModel

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

## Building from Source

```shell
# Build iOS artifacts

# Download freetype library to external directory
git submodule update --init --recursive
cd external/freetype

# Build FreeType library
./build-ios-cmake.sh
```

## Version History

- **0.1.0** - Initial release with basic LaTeX mathematical expression rendering support

## Acknowledgments

This project is developed based on the [AndroidMath](https://github.com/gregcockroft/AndroidMath)
project. Thanks to the original authors for their contributions.

## Contributing

Issues and Pull Requests are welcome to help improve this project.

## Contact

For questions or suggestions, please contact us through GitHub Issues.
