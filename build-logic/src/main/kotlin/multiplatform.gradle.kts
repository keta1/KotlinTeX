plugins {
    `kotlin-multiplatform`
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }


    iosX64()
    iosArm64()
    iosSimulatorArm64()
}