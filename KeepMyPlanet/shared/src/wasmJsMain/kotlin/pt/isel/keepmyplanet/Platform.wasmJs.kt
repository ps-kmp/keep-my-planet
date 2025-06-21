package pt.isel.keepmyplanet

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual val BASE_URL: String = "http://localhost:$SERVER_PORT"
