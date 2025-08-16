import org.gradle.internal.os.OperatingSystem
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.org.apache.commons.lang3.ArchUtils

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        sourceSets.commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        commonMain.dependencies {
            implementation(project(":katex-core"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Koin
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.bundles.koin)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            // 检测平台
            val currentOs = OperatingSystem.current()
            val lwjglPlatform = when {
                currentOs.isMacOsX -> "macos"
                currentOs.isWindows -> "windows"
                currentOs.isLinux -> "linux"
                else -> error("Unsupported OS: $currentOs")
            }
            val processor = ArchUtils.getProcessor()
            val lwjglArch = when {
                processor.isAarch64 -> "arm64"
                processor.is64Bit -> ""
                else -> error("Unsupported architecture: ${processor.arch} ${processor.type}")
            }
            val lwjglNatives = "natives-$lwjglPlatform-$lwjglArch".trim('-')
            // 平台特定的本地库
            implementation("org.lwjgl:lwjgl:${libs.versions.lwjgl.get()}:$lwjglNatives")
            implementation("org.lwjgl:lwjgl-freetype:${libs.versions.lwjgl.get()}:$lwjglNatives")
        }
    }
}

android {
    namespace = "io.github.darriousliu.katex"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.darriousliu.katex"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    kspCommonMainMetadata(libs.koin.ksp.compiler)
}

tasks.configureEach {
    if (name.startsWith("ksp") && name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

compose.desktop {
    application {
        mainClass = "io.github.darriousliu.katex.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.darriousliu.katex"
            packageVersion = "1.0.0"
        }
    }
}

