import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// ---------------------------------------------------------------------------
// Supabase settings -> generated AppConfig object.
// Values come from environment variables (CI) or a local ".env" file in the repo root.
// ---------------------------------------------------------------------------
val appConfigDir = layout.buildDirectory.dir("generated/appconfig/kotlin")
val generateAppConfig = tasks.register("generateAppConfig") {
    val envFile = rootProject.file(".env")
    val exampleFile = rootProject.file(".env.example")
    val outDir = appConfigDir
    outputs.dir(outDir)
    outputs.upToDateWhen { false }
    doLast {
        fun readEnv(file: File): Map<String, String> =
            if (!file.exists()) emptyMap()
            else file.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains("=") }
                .associate { it.substringBefore("=").trim() to it.substringAfter("=").trim().trim('"') }

        val fileValues = readEnv(exampleFile) + readEnv(envFile)
        fun pick(key: String): String =
            System.getenv(key)?.takeIf { it.isNotBlank() } ?: fileValues[key].orEmpty()
        fun esc(v: String): String =
            v.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$")

        val dir = outDir.get().asFile.resolve("com/example")
        dir.mkdirs()
        dir.resolve("AppConfig.kt").writeText(
            """
            package com.example

            object AppConfig {
                const val SUPABASE_URL: String = "${esc(pick("SUPABASE_URL"))}"
                const val SUPABASE_PUBLISHABLE_KEY: String = "${esc(pick("SUPABASE_PUBLISHABLE_KEY"))}"
            }
            """.trimIndent() + "\n"
        )
    }
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("wapiApp")
        browser {
            commonWebpackConfig {
                outputFileName = "wapiApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            kotlin.srcDir(generateAppConfig)
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(libs.material.icons.extended)
                implementation(libs.jetbrains.lifecycle.viewmodel.compose)
                implementation(libs.jetbrains.lifecycle.runtime.compose)
                implementation(libs.jetbrains.navigation.compose)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.supabase.postgrest)
                implementation(libs.supabase.auth)
                implementation(libs.supabase.storage)
                implementation(libs.ktor.client.core)
            }
        }
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.ktor.client.android)
        }
        wasmJsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}

tasks.configureEach {
    if (name.startsWith("compile") && name.contains("Kotlin")) dependsOn(generateAppConfig)
}

compose.resources {
    packageOfResClass = "com.example.resources"
    generateResClass = always
}

android {
    namespace = "com.example.shared"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
