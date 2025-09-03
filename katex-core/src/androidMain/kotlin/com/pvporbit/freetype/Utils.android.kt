package com.pvporbit.freetype

import android.content.Context
import io.github.darriousliu.katex.core.resources.Res

lateinit var appContext: Context

actual fun readAssetFile(path: String): ByteArray {
    return try {
        val realPath = Res.getUri(path)
        val relativePath = realPath.removePrefix("file:///android_asset/")
        val assetManager = appContext.assets
        assetManager.open(relativePath).use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        byteArrayOf()
    }
}
