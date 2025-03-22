package pt.isel.keepmyplanet

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform