package io.github.mahmoud.ktorscope

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform