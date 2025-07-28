package com.mrl.katex.core

import android.content.Context
import androidx.startup.Initializer
import com.pvporbit.freetype.appContext

class AppContextInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        appContext = context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}