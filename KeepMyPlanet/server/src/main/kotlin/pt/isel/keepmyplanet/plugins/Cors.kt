package pt.isel.keepmyplanet.plugins

import io.ktor.server.application.*
import io.ktor.server.application.install
import io.ktor.http.HttpMethod
import io.ktor.server.plugins.cors.routing.CORS


fun Application.configureCors() {
    install(CORS) {
        //allowHost("localhost:3000", schemes = listOf("http"))
        anyHost()
        allowHeader("Content-Type")
        allowHeader("Authorization")
        allowCredentials = true
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Options)
    }
}

/*
fun Application.configureCors() {
    install(CORS) {
        anyHost() // Ou .allowHost("localhost:8080")
        allowHeader("Content-Type")
        allowHeader("Authorization")
        allowCredentials = true
        allowMethod(io.ktor.http.HttpMethod.Get)
        allowMethod(io.ktor.http.HttpMethod.Post)
        allowMethod(io.ktor.http.HttpMethod.Put)
        allowMethod(io.ktor.http.HttpMethod.Delete)
        allowMethod(io.ktor.http.HttpMethod.Options)
    }
}*/
