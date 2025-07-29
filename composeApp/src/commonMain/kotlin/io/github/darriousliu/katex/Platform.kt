package io.github.darriousliu.katex

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform