package com.pvporbit.freetype

class CharMap(pointer: Long) : Utils.Pointer(pointer) {
    companion object {
        fun getCharMapIndex(charmap: CharMap): Int {
            return FreeType.getCharMapIndex(charmap.pointer)
        }
    }
}