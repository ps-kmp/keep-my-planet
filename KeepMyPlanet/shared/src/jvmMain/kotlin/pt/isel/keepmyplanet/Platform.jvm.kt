package pt.isel.keepmyplanet

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual val BASE_URL: String = "http://localhost:$SERVER_PORT"
