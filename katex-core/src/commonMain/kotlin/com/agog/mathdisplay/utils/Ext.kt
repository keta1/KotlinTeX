package com.agog.mathdisplay.utils

// 保留n位小数
internal fun Float.toFixed(n: Int): String {
    val str = this.toString()
    val dotIndex = str.indexOf('.')
    return if (dotIndex == -1) {
        str + "." + "0".repeat(n)
    } else {
        val decimalPart = str.substring(dotIndex + 1)
        if (decimalPart.length >= n) {
            str.substring(0, dotIndex + 1) + decimalPart.substring(0, n)
        } else {
            str + "0".repeat(n - decimalPart.length)
        }
    }
}