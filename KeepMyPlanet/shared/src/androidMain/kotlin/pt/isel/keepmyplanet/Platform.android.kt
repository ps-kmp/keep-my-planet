package pt.isel.keepmyplanet

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual val BASE_URL: String = "http://10.0.2.2:$SERVER_PORT"
