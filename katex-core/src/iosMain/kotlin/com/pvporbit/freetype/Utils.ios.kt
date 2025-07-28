package com.pvporbit.freetype

import io.github.mrl.katex.core.resources.Res
import kotlinx.coroutines.runBlocking

actual fun readAssetFile(path: String): ByteArray {
    try {
        return runBlocking { Res.readBytes(path) }
    } catch (e: Exception) {
        println(e)
    }
    return byteArrayOf()
}