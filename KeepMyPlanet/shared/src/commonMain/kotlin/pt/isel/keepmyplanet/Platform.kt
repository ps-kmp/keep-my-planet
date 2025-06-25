package pt.isel.keepmyplanet

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

const val SERVER_PORT = 1904
expect val BASE_URL: String

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = "Hello, ${platform.name}!"
}
