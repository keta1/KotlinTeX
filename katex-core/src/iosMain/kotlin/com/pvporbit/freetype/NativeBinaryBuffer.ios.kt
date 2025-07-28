@file:OptIn(ExperimentalForeignApi::class)

package com.pvporbit.freetype

import kotlinx.cinterop.*
import platform.posix.memcpy

actual class NativeBinaryBuffer(actual val size: Int) {
    var byteArray: ByteArray? = null
        private set
    var ptr: CPointer<ByteVar>? = null
        private set
    private var offset = 0
    private var limit = size

    init {
        byteArray = ByteArray(size)

        ptr = nativeHeap.allocArray(size)
        require(ptr != null) { "malloc failed" }
    }

    actual val short: Short
        get() {
            require(offset + 1 < limit) { "Buffer underflow" }
            val p = ptr!!
            val v = ((p[offset].toInt() and 0xFF) shl 8) or
                    (p[offset + 1].toInt() and 0xFF)
            offset += 2
            return v.toShort()
        }

    actual val int: Int
        get() {
            require(offset + 3 < limit) { "Buffer underflow" }
            val p = ptr!!
            println("int: ${p[offset].toInt()} ${p[offset + 1].toInt()} ${p[offset + 2].toInt()} ${p[offset + 3].toInt()}")
            val v = ((p[offset].toInt() and 0xFF) shl 24) or
                    ((p[offset + 1].toInt() and 0xFF) shl 16) or
                    ((p[offset + 2].toInt() and 0xFF) shl 8) or
                    (p[offset + 3].toInt() and 0xFF)
            offset += 4
            return v
        }


    actual fun position(): Int {
        return offset
    }

    actual fun position(newPosition: Int) {
        require(newPosition in 0..limit) { "Invalid position" }
        offset = newPosition
    }

    actual fun limit(newLimit: Int) {
        require(newLimit in 0..size) { "Invalid limit value" }
        limit = newLimit
        if (offset > limit) offset = limit
    }

    actual fun remaining(): Int {
        return limit - offset
    }

    actual fun fill(bytes: ByteArray) {
        require(bytes.size <= size)
        val outPtr = ptr!!
        bytes.usePinned { pinned ->
            memcpy(outPtr, pinned.addressOf(0), bytes.size.convert())
        }
    }

    actual fun toByteArray(length: Int): ByteArray {
        require(length <= size)
        val outPtr = ptr!!
        return ByteArray(length).apply {
            usePinned { pinned ->
                memcpy(pinned.addressOf(0), outPtr, length.convert())
            }
        }
    }

    actual fun free() {
        ptr?.let {
            nativeHeap.free(it)
            ptr = null
        }
    }
}