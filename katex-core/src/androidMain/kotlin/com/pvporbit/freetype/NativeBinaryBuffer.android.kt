package com.pvporbit.freetype

import java.nio.ByteBuffer

actual class NativeBinaryBuffer constructor(
    val byteBuffer: ByteBuffer
) {
    actual val size: Int
        get() = byteBuffer.capacity()
    actual val short: Short
        get() = byteBuffer.short
    actual val int: Int
        get() = byteBuffer.int

    actual fun position(): Int {
        return byteBuffer.position()
    }

    actual fun position(newPosition: Int) {
        byteBuffer.position(newPosition)
    }

    actual fun limit(newLimit: Int) {
        byteBuffer.limit(newLimit)
    }

    actual fun remaining(): Int {
        return byteBuffer.remaining()
    }


    actual fun fill(bytes: ByteArray) {
        require(bytes.size <= size)
        byteBuffer.position(0)
        byteBuffer.put(bytes)
        byteBuffer.position(0)
    }

    actual fun toByteArray(length: Int): ByteArray {
        require(length <= size)
        val arr = ByteArray(length)
        byteBuffer.position(0)
        byteBuffer.get(arr, 0, length)
        byteBuffer.position(0)  // 复用
        return arr
    }

    actual fun free() {
    }
}