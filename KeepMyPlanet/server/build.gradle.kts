plugins {
    alias(libs.plugins.kotlin.jvm)
    application
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.ktlint)
}

group = "pt.isel.keepmyplanet"
version = "1.0.0"

application {
    mainClass.set("pt.isel.keepmyplanet.ApplicationKt")
    applicationDefaultJvmArgs =
        listOf("-Dio.ktor.development=${extra["io.ktor.development"] ?: "false"}")
}

ktlint {
    android.set(true)
    filter {
        exclude {
            it.file.toPath().startsWith(
                project.layout.buildDirectory
                    .get()
                    .asFile
                    .toPath(),
            )
        }
    }
}

dependencies {
    // project dependencies
    implementation(projects.shared)

    // ktor
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.call.logging.jvm)
    implementation(libs.ktor.server.status.pages.jvm)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.sse)
    implementation(libs.ktor.server.cors)

    // database
    implementation(libs.sqldelight.runtime)
    implementation(libs.sqldelight.jdbc.driver)
    implementation(libs.postgresql)
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
            dialect(libs.sqldelight.postgresql.dialect)
        }
    }
}
