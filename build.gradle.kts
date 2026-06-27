// Central build script: runs once per Stonecutter node (e.g. 1.21.11-fabric, 26.1.2-neoforge).
// ModStitch reads the node's gradle.properties (modstitch.platform) to pick the toolchain:
// Fabric Loom for Fabric nodes, NeoForge ModDevGradle for NeoForge nodes.

plugins {
    id("dev.isxander.modstitch.base") version "0.8.5"
}

fun prop(name: String, consumer: (String) -> Unit) {
    (findProperty(name) as? String)?.let(consumer)
}

val minecraft = property("deps.minecraft") as String
val loader = name.substringAfterLast("-")          // "1.21.11-fabric" -> "fabric"
val javaRelease = when {
    minecraft.startsWith("26.") -> 25              // 26.1+ requires Java 25
    minecraft == "1.20.1" -> 17                    // 1.20.1 is Java 17
    else -> 21                                     // 1.21.x is Java 21
}

version = property("mod_version") as String
base.archivesName.set("minestratorhelper-$loader-$minecraft")
// Drop the mod version from the jar file name so release download links stay stable,
// e.g. .../releases/latest/download/minestratorhelper-fabric-1.21.11.jar always resolves.
tasks.withType<AbstractArchiveTask>().configureEach { archiveVersion.set("") }

modstitch {
    minecraftVersion = minecraft

    // Fills the fabric.mod.json / neoforge.mods.toml templates under src/main/templates/.
    metadata {
        modId = "minestratorhelper"
        modName = "Minestrator Helper"
        modVersion = property("mod_version") as String
        modGroup = "fr.minestrator"
        modAuthor = "Minestrator"
        replacementProperties.putAll(
            mapOf(
                "mod_description" to "Integration of Minestrator-hosted servers directly in Minecraft",
                "mod_license" to "All Rights Reserved",
                "minecraft" to minecraft,
                "minecraft_version_range" to ">=$minecraft",
                "neoforge_loader_range" to "[1,)"
            )
        )
    }

    // Active when modstitch.platform = fabric-loom[-remap]
    loom {
        fabricLoaderVersion = property("deps.fabric_loader") as String
    }

    // Active when modstitch.platform = moddevgradle
    moddevgradle {
        prop("deps.neoforge") { neoForgeVersion = it }
        defaultRuns()
    }
}

// Feed the active loader to Stonecutter so //? if fabric { … } / neoforge blocks resolve.
stonecutter {
    constants.match(loader, "fabric", "neoforge")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(javaRelease)) }
}

tasks.withType<JavaCompile> {
    options.release.set(javaRelease)
    dependsOn("stonecutterGenerate") // resolve //? blocks before compiling
}

dependencies {
    // Fabric nodes: Fabric API + Architectury API (Fabric flavour)
    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")
        modstitchModImplementation("dev.architectury:architectury-fabric:${property("deps.architectury")}")
    }
    // NeoForge nodes: Architectury API (NeoForge flavour)
    modstitch.moddevgradle {
        modstitchModImplementation("dev.architectury:architectury-neoforge:${property("deps.architectury")}")
    }
}
