plugins {
    // core
    alias(libs.plugins.kotlinJvm)
    application

    // feature
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
}

group = "pt.isel.keepmyplanet"
version = "1.0.0"

application {
    mainClass.set("pt.isel.keepmyplanet.ApplicationKt")
    applicationDefaultJvmArgs =
        listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

dependencies {
    // project dependencies
    implementation(projects.shared)

    // ktor core
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.config.yaml)

    // ktor features
    implementation(libs.ktor.server.call.logging.jvm)
    implementation(libs.ktor.server.status.pages.jvm)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.ktor.server.sse)

    // serialization
    implementation(libs.ktor.serialization.kotlinx.json)

    // logging
    implementation(libs.logback)

    // testing
    testImplementation(libs.ktor.server.test.host.jvm)
    testImplementation(libs.kotlin.test.junit)
}
