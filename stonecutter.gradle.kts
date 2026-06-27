plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.11-fabric"

// Builds every node (all loader × version combinations) into each node's build/libs.
tasks.register("chiseledBuild") {
    group = "project"
    description = "Builds ALL Stonecutter nodes (every loader × version)."
    dependsOn(stonecutter.tasks.named("build"))
}

// Shared repositories for every node so dependency resolution works on all loaders.
allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.fabricmc.net/")              // Fabric Loader / API
        maven("https://maven.neoforged.net/releases")     // NeoForge / MDG
        maven("https://maven.architectury.dev/")          // Architectury API
        maven("https://maven.isxander.dev/releases")      // ModStitch runtime bits
    }
}
