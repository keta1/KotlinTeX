package io.github.mrl.katex

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform