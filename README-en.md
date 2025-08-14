# KotlinTeX

[English Version](README-en.md) | [中文版本](README.md)

[![License](https://img.shields.io/badge/License-BSD%202--Clause-orange.svg)](https://opensource.org/licenses/BSD-2-Clause)
[![Kotlin](https://img.shields.io/badge/kotlin-multiplatform-blue.svg?logo=kotlin)]([http://kotlinlang.org](https://www.jetbrains.com/kotlin-multiplatform/))

A cross-platform LaTeX mathematical expression rendering library based on Kotlin Multiplatform,
supporting Android / iOS / Jvm platforms.

## Display

https://github.com/user-attachments/assets/1f8a57a2-8610-44c8-b33d-4a1b4dbf6a34

## About the Project

This project is rewritten based on the open-source project
[**AndroidMath**](https://github.com/gregcockroft/AndroidMath),
converting all code to Kotlin and implementing cross-platform support using Kotlin Multiplatform
technology. It provides high-quality LaTeX mathematical expression rendering functionality for
Android / iOS / Jvm platforms through Compose Multiplatform.

## Features

- 🚀 Based on Kotlin Multiplatform technology
    - Android integrates FreeType library via JNI
    - iOS integrates FreeType library via C interop
    - JVM platform integrates FreeType library via lwjgl library
- 📱 Supports Android / iOS / Jvm platforms
- 🎨 UI rendering using Compose Multiplatform
- 📊 Complete LaTeX mathematical expression support
- 🔧 Easy integration and usage

## Platform Support

- ✅ Android (API 23+), already adapted for 16KB Page Size
- ✅ iOS (iOS 13+)
- ✅ JVM (Compose MultiplatformDesktop applications)

## Dependencies

### Gradle (Kotlin DSL)

Add the following to your `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        mavenCentral() // or maven { url = uri("https://jitpack.io") }
    }
}
```

Then, add the dependency in your `build.gradle.kts`:

### Android

```kotlin
dependencies {
    implementation("io.github.darriousliu:katex:0.2.1")
}
```

### Kotlin Multiplatform

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.darriousliu:katex:0.2.1")
        }
    }
}
```

### Project Configuration

Please ensure your project is properly configured with Kotlin Multiplatform and Compose
Multiplatform.

### JVM Platform Specific Configuration

When using on the JVM platform, you need to add platform-specific FreeType native library
dependencies:

```kotlin
jvmMain.dependencies {
    // Detect platform
    val lwjglNatives = when (System.getProperty("os.name")) {
        "Mac OS X" -> when (System.getProperty("os.arch")) {
            "aarch64" -> "natives-macos-arm64"
            else -> "natives-macos-x64"
        }
        "Linux" -> when (System.getProperty("os.arch")) {
            "aarch64" -> "natives-linux-arm64"
            else -> "natives-linux-x64"
        }
        else -> when (System.getProperty("os.arch").contains("64")) {
            true -> "natives-windows-x64"
            false -> "natives-windows-x86"
        }
    }
    // Platform-specific native libraries
    runtimeOnly("org.lwjgl:lwjgl:version:$lwjglNatives")
    runtimeOnly("org.lwjgl:lwjgl-freetype:version:$lwjglNatives")
}
```

## Usage

1. Direct usage with LaTeX string

```kotlin
@Composable
fun LatexExample() {
    MTMathView(
        latex = "\\frac{a}{b}",
        modifier = Modifier.fillMaxWidth(),
        fontSize = 20.sp,
        textColor = Color.Black,
        font = null,
        mode = MTMathViewMode.KMTMathViewModeDisplay,
        textAlignment = MTTextAlignment.KMTTextAlignmentLeft,
        displayErrorInline = true,
        errorFontSize = 20.sp,
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
                fontSize = 20.sp,
                textColor = Color.Black,
                font = null,
                mode = MTMathViewMode.KMTMathViewModeDisplay,
                textAlignment = MTTextAlignment.KMTTextAlignmentLeft,
                displayErrorInline = false,
                errorFontSize = 20.sp,
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

### CI/CD Configuration Example

You can use the following configuration to build artifacts for different platforms in a CI/CD
environment:

```kotlin
// build.gradle.kts

val targetOs = findProperty("targetOs") ?: "linux" // or "windows" or "macos"
val targetArch = findProperty("targetArch") ?: "x64" // or "arm64"
val lwjglVersion = "3.3.6"

kotlin {
    sourceSets {
        jvmMain.dependencies {
            // Determine native library dependencies based on target and arch parameters
            val nativeTarget = "natives-$targetOs-$targetArch"

            if (nativeTarget != null) {
                runtimeOnly("org.lwjgl:lwjgl:$lwjglVersion:$nativeTarget")
                runtimeOnly("org.lwjgl:lwjgl-freetype:$lwjglVersion:$nativeTarget")
            }
        }
    }
}
```

To distribute JVM applications for different platforms, you can set the `targetOs` and `targetArch`
parameters in the CI/CD environment to automatically select the appropriate native libraries.

```shell
./gradlew ... -PtargetOs=linux -PtargetArch=x64
./gradlew ... -PtargetOs=windows -PtargetArch=x64
./gradlew ... -PtargetOs=macos -PtargetArch=arm64
./gradlew ... -PtargetOs=macos -PtargetArch=x64
```

## Acknowledgments

This project is developed based on the [AndroidMath](https://github.com/gregcockroft/AndroidMath)
project. Thanks to the original authors for their contributions.

## Contributing

Issues and Pull Requests are welcome to help improve this project.

## Contact

For questions or suggestions, please contact us through GitHub Issues.
