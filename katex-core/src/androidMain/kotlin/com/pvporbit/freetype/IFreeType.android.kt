package com.pvporbit.freetype

private val singleton by lazy { FreeTypeAndroid() }

actual val FreeType: IFreeType = singleton