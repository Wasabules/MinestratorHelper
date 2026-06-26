# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A **client-side Minecraft mod** that integrates Minestrator-hosted servers into the game (a "My Servers" screen with start/stop/join, an in-game console screen, client commands, an F6 keybind). It talks to the Minestrator REST API (`https://mine.sttr.io`) with a Bearer token.

It is built as a **single codebase that targets multiple mod loaders (Fabric + NeoForge) and multiple Minecraft versions** from one source tree.

## Toolchain (do not "upgrade" casually — these versions are interdependent)

| Component | Version | Why pinned |
|---|---|---|
| Gradle | **9.6.1** | MC 1.21.11 is non-obfuscated → needs Loom ≥1.14 → needs Gradle ≥9.2 |
| Architectury Loom | **1.17-SNAPSHOT** | multi-loader Loom; needs Gradle 9 |
| Architectury Plugin | **3.5-SNAPSHOT** | 3.4 crashes on Loom 1.17 (`LoomInterface11` / `RunConfig` NoClassDefFound) |
| Shadow | **com.gradleup.shadow 9.4.2** | johnrengelman shadow 8.x is incompatible with Gradle 9 |
| Stonecutter | **0.6** (`dev.kikugie.stonecutter`) | multi-version preprocessor; 0.6 DSL (`branch()`) still works under Gradle 9 |
| Mappings | **Mojang official (Mojmap)** | identical class/method names on Fabric & NeoForge → the `common` module is shareable as-is |

**JDK**: the build must run on **JDK 21** (MC 1.21.x is Java 21; Loom is calibrated for it). The repo relies on `org.gradle.java.home` in the *user's global* `~/.gradle/gradle.properties` pointing at a JDK 21 install — that path is machine-specific and breaks when the JDK is updated. JDK 17 (for 1.20.1) is auto-resolved via toolchains.

## Module & version layout

Two orthogonal axes: **Stonecutter** handles Minecraft versions, **Architectury** handles loaders.

```
minestratorhelper/
├── settings.gradle.kts        # Stonecutter declares versions + branches (fabric, neoforge)
├── stonecutter.gradle.kts     # controller: active version marker + chiseledBuild tasks
├── build.gradle.kts           # the COMMON module (root src/ = common, loader-agnostic)
├── gradle.properties          # mod.* metadata + loader-agnostic deps
├── buildSrc/                  # ModData/prop() helpers
├── versions/<mc>/gradle.properties   # per-version deps (Fabric API, NeoForge, Architectury API). GENERATED tree under versions/<mc>/ is .gitignored
├── src/                       # ░ COMMON CODE — all loaders, all versions ░
│   └── main/java/fr/minestrator/helper/
│       ├── MinestratorHelper.java     # init() entry, called by each loader
│       ├── api/                       # ApiClient (HTTP/REST) + DTOs
│       ├── config/ModConfig.java      # token store; config dir via dev.architectury.platform.Platform
│       ├── screen/                    # *Logic (business), ServerStateManager, + Mojmap Screens/Widgets
│       └── client/                    # Commands, KeyBinds, ScreenButtons (registered via Architectury events)
├── fabric/                    # Fabric entrypoint (ClientModInitializer → MinestratorHelper.init())
└── neoforge/                  # NeoForge entrypoint (@Mod(dist=CLIENT) → MinestratorHelper.init())
```

Matrix: **1.20.1 = Fabric only** (NeoForge starts at MC 1.20.2); **1.21.1 and 1.21.11 = Fabric + NeoForge**.

### Architecture pattern

All business logic, API, config, state, AND the GUI (Screens/Widgets in Mojmap) live in `common`. The two loader modules are thin: an entrypoint that calls `MinestratorHelper.init()`. Cross-loader concerns use **Architectury API** instead of mixins:
- Client commands → `ClientCommandRegistrationEvent`
- Keybind → `KeyMappingRegistry` + `ClientTickEvent.CLIENT_POST`
- Buttons injected into vanilla screens → `ClientGuiEvent.INIT_POST` (replaces the old `MultiplayerScreenMixin`/`GameMenuScreenMixin`)
- Config dir / platform info → `dev.architectury.platform.Platform`
- Logging → SLF4J via `MinestratorHelper.LOGGER`

There are currently **no mixins** and the access widener is empty.

## Build & run

The active Minecraft version (in `stonecutter.gradle.kts`, `stonecutter active "..."`) is the one Stonecutter preprocesses `src/` for. **Only the active version compiles**; targeting `:1.20.1:compileJava` while another version is active is a no-op.

```bash
# Switch which version is "active" (rewrites //? comments in src/)
./gradlew "Set active project to 1.21.1"     # or 1.20.1 / 1.21.11
./gradlew "Reset active project"             # back to vcs default (1.20.1) — run before committing
./gradlew "Refresh active project"           # re-process //? comments without switching

# Compile/build the COMMON for the active version (fast feedback on GUI/Mojmap code):
./gradlew :<active-version>:compileJava

# Build a single loader node to a jar:
./gradlew :fabric:1.21.1:build    ./gradlew :neoforge:1.21.11:build

# Build EVERYTHING (all version×loader jars → build/libs/<modver>/<loader>/):
./gradlew chiseledBuild
./gradlew chiseledBuildFabric     ./gradlew chiseledBuildNeoforge

# Run a dev client of the active version:
./gradlew runActiveClientFabric   ./gradlew runActiveClientNeoforge
```

No tests exist.

## Handling version differences — Stonecutter `//?`

Because `common` is one source tree compiled against several MC versions (in Mojmap), API differences are bridged with Stonecutter preprocessor comments. The active block is plain code; the inactive block is wrapped in `/* */`; Stonecutter flips them on version switch. Predicate examples in use:

- `//? if >=1.21` — `ObjectSelectionList` ctor (5-arg vs 6-arg top/bottom in 1.20.1); `ServerData`/`ConnectScreen.startConnecting` arg shape.
- `//? if >=1.21.9` — `KeyMapping.Category` (vs `String` category) — note 1.21.11 renames `ResourceLocation` → `net.minecraft.resources.Identifier`.
- `//? if >=1.21.11` — list entries override `renderContent(GuiGraphics,int,int,boolean,float)` + `getX/getY` (vs the 10-arg `render(...)`); `mouseClicked(MouseButtonEvent, boolean)` (vs `(double,double,int)`).

When adding a feature: write it for the active version, `./gradlew :<v>:compileJava`, then switch active to each other version and add `//?` where the compiler reports signature mismatches. Keep shared bodies in a helper method (e.g. `renderRow`) so only the differing signature lives inside the `//?`.

To discover the exact Mojmap signature for a given MC version, inspect the cached jar, e.g.:
`javap -classpath ~/.gradle/caches/fabric-loom/<mc>/.../minecraft-merged-mojang.jar 'net.minecraft.client.KeyMapping$Category'`

## Minestrator API notes

- Bearer token from `ModConfig`; empty token ⇒ `isConfigured()` false ⇒ calls short-circuit.
- Responses nested under `api.data.*`; `ApiClient` walks them with Gson `JsonObject`. Booleans arrive as integer `0`/`1`.
- Endpoints: `GET /user/`, `GET /user/{id}/servers`, `GET /server/{id}/live`, `PUT /server/{id}/poweraction` (start/stop/restart/kill), `PUT /server/{id}/command`. User id is fetched once and cached.
- All calls return `CompletableFuture` off-thread; marshal UI updates back with `Minecraft.getInstance().execute(...)`.
