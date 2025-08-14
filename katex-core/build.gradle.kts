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
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.startup)
            }
        }

        iosMain {
            dependencies {

            }
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)

            // LWJGL 核心依赖
            implementation(libs.lwjgl)
            implementation(libs.lwjgl.freetype)
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
