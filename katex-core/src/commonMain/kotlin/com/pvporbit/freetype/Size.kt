package com.pvporbit.freetype

class Size(pointer: Long) : Utils.Pointer(pointer) {
    val metrics: SizeMetrics?
        get() {
            val sizeMetrics = FreeType.sizeGetMetrics(pointer)
            if (sizeMetrics <= 0) return null
            return SizeMetrics(sizeMetrics)
        }
}