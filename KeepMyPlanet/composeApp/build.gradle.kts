import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
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

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    jvm("desktop")

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "composeApp"
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer =
                    (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                        static =
                            (static ?: mutableListOf()).apply {
                                add(project.file("src/wasmJsMain/resources").path)
                                add(
                                    project.layout.buildDirectory.asFile
                                        .get()
                                        .resolve("dist/wasmJs/developmentExecutable")
                                        .path,
                                )
                            }
                    }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                // compose core
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)

                // compose utilities
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

                // projects
                implementation(projects.shared)

                // ktor
                implementation(project.dependencies.platform(libs.ktor.bom))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.auth)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.sse)

                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.multiplatform.settings.coroutines)
                implementation(libs.mapcompose.mp)
                implementation(libs.sqldelight.coroutines.extensions)
                implementation(libs.coil.compose)
                implementation(libs.coil.network.ktor3)
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(compose.preview)
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
                implementation(libs.ktor.client.android)
                implementation(libs.sqldelight.android.driver)

                // barcode and qr code scanning
                implementation(libs.zxing.core)
                implementation(libs.mlkit.barcode.scanning)
                implementation(libs.accompanist.permissions)
                implementation(libs.bundles.androidx.camera)
                implementation(libs.google.playservices.location)
                implementation(project.dependencies.platform(libs.firebase.bom))
                implementation(libs.firebase.messaging)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutines.swing)
                implementation(libs.sqldelight.sqlite.driver)
                implementation(libs.ktor.client.java)
                implementation(libs.logback.classic)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
                implementation(libs.sqldelight.web.worker.driver)
            }
        }
    }
}

android {
    namespace = "pt.isel.keepmyplanet"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()
    defaultConfig {
        applicationId = "pt.isel.keepmyplanet"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    signingConfigs {
        create("release") {
            val keystoreFile = project.file("my-release-key.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword =
                    System.getenv("MY_RELEASE_STORE_PASSWORD")
                        ?: extra["MY_RELEASE_STORE_PASSWORD"] as? String
                keyAlias =
                    System.getenv("MY_RELEASE_KEY_ALIAS")
                        ?: extra["MY_RELEASE_KEY_ALIAS"] as? String
                keyPassword =
                    System.getenv("MY_RELEASE_KEY_PASSWORD")
                        ?: extra["MY_RELEASE_KEY_PASSWORD"] as? String
            } else {
                println(
                    "Warning: 'my-release-key.keystore' not found in composeApp/. " +
                        "Release build will be signed with the debug key as a fallback.",
                )
            }
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            val keystoreFile = project.file("my-release-key.jks")
            signingConfig =
                if (keystoreFile.exists()) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "pt.isel.keepmyplanet.MainKt"

        jvmArgs("--add-opens", "java.desktop/sun.awt=ALL-UNNAMED")
        jvmArgs("--add-opens", "java.desktop/java.awt.peer=ALL-UNNAMED")

        if (System.getProperty("os.name").contains("Mac")) {
            jvmArgs("--add-opens", "java.desktop/sun.lwawt=ALL-UNNAMED")
            jvmArgs("--add-opens", "java.desktop/sun.lwawt.macosx=ALL-UNNAMED")
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.AppImage)
            modules("java.sql", "java.net.http")
            packageName = "pt.isel.keepmyplanet"
            packageVersion = "1.0.0"
        }
    }
}

sqldelight {
    databases {
        create("KeepMyPlanetCache") {
            packageName.set("pt.isel.keepmyplanet.cache")
        }
    }
}
