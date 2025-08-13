plugins {
    id("multiplatform")
    id("publish")
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    androidTarget {
        publishLibraryVariants("release")
    }
    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate

    listOf(
        iosArm64(),
        iosX64(),
        iosSimulatorArm64(),
    ).forEach {
        it.binaries.framework {
            baseName = "freetype"
            isStatic = true
        }
        it.compilations {
            @Suppress("unused")
            val main by getting {
                cinterops {
                    val freetype by creating {
                        defFile(file("src/nativeInterop/cinterop/freetype.def"))
                        includeDirs("../external/freetype/include")
                    }
                }
            }
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                // Add KMP dependencies here
                implementation(compose.components.resources)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.material3)

                implementation(libs.androidx.collection)

                implementation(libs.okio)

                implementation(project.dependencies.platform(libs.kotlinx.coroutines.bom))
                implementation(libs.kotlinx.coroutines.core)
            }
        }
        androidMain {
            dependencies {
                // Add Android-specific dependencies here. Note that this source set depends on
                // commonMain by default and will correctly pull the Android artifacts of any KMP
                // dependencies declared in commonMain.
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.startup)
            }
        }

        iosMain {
            dependencies {
                // Add iOS-specific dependencies here. This a source set created by Kotlin Gradle
                // Plugin (KGP) that each specific iOS target (e.g., iosX64) depends on as
                // part of KMP’s default source set hierarchy. Note that this source set depends
                // on common by default and will correctly pull the iOS artifacts of any
                // KMP dependencies declared in commonMain.
            }
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // 检测平台
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
            // LWJGL 核心依赖
            implementation(libs.lwjgl)
            implementation(libs.lwjgl.freetype)

            // 平台特定的本地库
            runtimeOnly("org.lwjgl:lwjgl:${libs.versions.lwjgl.get()}:$lwjglNatives")
            runtimeOnly("org.lwjgl:lwjgl-freetype:${libs.versions.lwjgl.get()}:$lwjglNatives")
        }
    }
}

compose.resources {
    packageOfResClass = "${project.group}.katex.core.resources"
}

android {
    namespace = "io.github.darriousliu.katex.core"
    compileSdk = 36
    ndkVersion = "28.1.13356709"

    defaultConfig {
        minSdk = 23

        consumerProguardFile("proguard-rules.pro")
    }

    externalNativeBuild {
        cmake {
            path = file("src/androidMain/cpp/CMakeLists.txt")
            version = "4.0.3"
        }
    }
}

mavenPublishing {
    coordinates(group.toString(), "katex-core", version.toString())
}
