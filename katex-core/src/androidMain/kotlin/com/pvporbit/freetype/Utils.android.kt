package com.pvporbit.freetype

import android.content.Context
import katex.katex_core.generated.resources.Res

lateinit var appContext: Context

actual fun readAssetFile(path: String): ByteArray {
    return try {
        val realPath = Res.getUri(path)
        val relativePath = if (realPath.startsWith("file:///android_asset/")) {
            realPath.removePrefix("file:///android_asset/")
        } else {
            realPath
        }
        val assetManager = appContext.assets
        assetManager.open(relativePath).use { inputStream ->
            inputStream.readBytes()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        byteArrayOf()
    }
}