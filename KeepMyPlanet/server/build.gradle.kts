plugins {
    // core
    alias(libs.plugins.kotlinJvm)
    application

    // feature
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
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
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)

    // ktor features
    implementation(libs.ktor.server.call.logging.jvm)
    implementation(libs.ktor.server.status.pages.jvm)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.sse)

    // database
    implementation(libs.sqldelight.runtime)
    implementation(libs.jdbc.driver)
    implementation(libs.postgresql)

    // connection pool
    implementation(libs.hikaricp)

    // serialization
    implementation(libs.ktor.serialization.kotlinx.json)

    // logging
    implementation(libs.logback.classic)

    // testing
    testImplementation(libs.ktor.server.test.host.jvm)
    testImplementation(libs.kotlin.test.junit)
}

sqldelight {
    databases {
        create("Database") {
            packageName.set("pt.isel.keepmyplanet.db")
            dialect(libs.postgresql.dialect)
        }
    }
}
