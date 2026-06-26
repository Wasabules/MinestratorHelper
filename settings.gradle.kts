pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev")
        maven("https://maven.neoforged.net/releases/")
        maven("https://maven.kikugie.dev/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.6"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    create(rootProject) {
        // Root `src/` acts as the 'common' project (shared, loader-agnostic).
        versions("1.20.1", "1.21.1", "1.21.11")
        branch("fabric")                               // Fabric on every version
        branch("neoforge") { versions("1.21.1", "1.21.11") } // NeoForge only >= 1.20.2
    }
}

rootProject.name = "minestratorhelper"
