package com.pvporbit.freetype

class SizeMetrics constructor(pointer: Long) : Utils.Pointer(pointer) {
    val ascender: Long
        get() = FreeType.sizeMetricsGetAscender(pointer)

    val descender: Long
        get() = FreeType.sizeMetricsGetDescender(pointer)

    val height: Long
        get() = FreeType.sizeMetricsGetHeight(pointer)

    val maxAdvance: Long
        get() = FreeType.sizeMetricsGetMaxAdvance(pointer)

    val xppem: Int
        get() = FreeType.sizeMetricsGetXPPEM(pointer)

    val yppem: Long
        get() = FreeType.sizeMetricsGetYPPEM(pointer)

    val xScale: Long
        get() = FreeType.sizeMetricsGetXScale(pointer)

    val yScale: Long
        get() = FreeType.sizeMetricsGetYScale(pointer)
}