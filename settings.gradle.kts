pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.isxander.dev/releases/")   // ModStitch
        maven("https://maven.fabricmc.net/")            // Fabric Loom
        maven("https://maven.neoforged.net/releases/")  // NeoForge ModDevGradle
        maven("https://maven.kikugie.dev/releases")      // Stonecutter
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    id("dev.kikugie.stonecutter") version "0.9.3"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        // Registers one node "<mc>-<loader>" per loader for a given MC version.
        fun mc(mcVersion: String, loaders: Iterable<String>) =
            loaders.forEach { version("$mcVersion-$it", mcVersion) }

        mc("1.20.1", listOf("fabric"))                  // NeoForge starts at 1.20.2
        mc("1.21.1", listOf("fabric", "neoforge"))
        mc("1.21.11", listOf("fabric", "neoforge"))
        // mc("26.1.2", listOf("fabric", "neoforge"))   // phase 2: needs the 26.1 GUI rendering port

        vcsVersion = "1.21.11-fabric"
    }
}

rootProject.name = "minestratorhelper"
