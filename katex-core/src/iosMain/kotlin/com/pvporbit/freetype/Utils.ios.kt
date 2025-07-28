package com.pvporbit.freetype

import katex.katex_core.generated.resources.Res
import kotlinx.coroutines.runBlocking

actual fun readAssetFile(path: String): ByteArray {
    try {
        return runBlocking { Res.readBytes(path) }
    } catch (e: Exception) {
        println(e)
    }
    return byteArrayOf()
}