# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A **client-side Minecraft mod** that integrates Minestrator-hosted servers into the game:
- a "My Servers" screen with start/stop/join and live monitoring gauges (CPU/RAM/disk/players);
- an in-game **live console** (F6): real-time logs with ANSI colours, word-wrap, mouse-wheel + draggable scrollbar, frozen-while-scrolled view, and per-level filter pills (INFO/WARN/ERROR);
- **client commands** `/reboot`, `/mstop`, `/mstart`, and `/sudo <command>` (runs a console command from chat, with tab-completion of popular commands and online players);
- an F6 keybind.

It talks to the Minestrator REST API (`https://mine.sttr.io`) with a Bearer token.

It is built as a **single codebase that targets multiple mod loaders (Fabric + NeoForge) and multiple Minecraft versions** from one source tree, using **Stonecutter** (version × loader preprocessing) + **ModStitch** (drives the official per-loader build tools).

## Toolchain (do not "upgrade" casually — these versions are interdependent)

| Component | Version | Role / why pinned |
|---|---|---|
| Gradle | **9.6.1** | runs on JDK 25; needed by ModStitch / Loom 1.15+ / MDG 2 |
| **ModStitch** | `dev.isxander.modstitch.base` **0.8.5** | one `modstitch { }` DSL that drives **official Fabric Loom** (Fabric) and **NeoForge ModDevGradle** (NeoForge), one platform active per node. Supports 26.1 (de-obfuscated MC) — which Architectury Loom does **not**. |
| Fabric Loom | official, via ModStitch | Fabric builds. `modstitch.platform=fabric-loom-remap` for obfuscated MC (≤1.21.11), `fabric-loom` (no remap) for 26.1+. |
| NeoForge ModDevGradle | official, via ModStitch | NeoForge builds. `modstitch.platform=moddevgradle`. |
| Stonecutter | **0.9.3** (`dev.kikugie.stonecutter`) | multi-version **and** multi-loader: one node per `<mc>-<loader>`. `constants.match(loader, …)` drives `//? if fabric / neoforge`. |
| foojay-resolver | **0.8.0** | auto-provisions JDK toolchains (17/21/25). |
| **Architectury API** | per-loader artifact (`architectury-fabric` / `architectury-neoforge`), version pinned per MC | **runtime** cross-loader event/abstraction library — **KEPT**. The migration only swapped the *build* tool (Architectury Loom → ModStitch); all `dev.architectury.*` event code is unchanged. |

> History: this project previously used **Architectury Loom + Plugin**. It was migrated to ModStitch because Architectury Loom cannot build MC **26.1+** (Minecraft is now shipped de-obfuscated → no Mojang mappings; Architectury Loom hasn't integrated the new mapping-free Loom — issue architectury/architectury-loom#328).

**JDK**: the Gradle daemon must run on **JDK 25** (ModStitch 0.8.5 requires it; 26.1+ targets Java 25). JDK 25 also compiles the Java 17 (1.20.1) and Java 21 (1.21.x) targets via per-node toolchains + `--release`. Pass `-Dorg.gradle.java.home="…/jdk-25…"` on each `gradlew` call, or set `org.gradle.java.home`. JDK 17/21 must also be available (toolchains) — foojay auto-downloads them if missing.

## Module & version layout

Two orthogonal axes, **both handled by Stonecutter** now: Minecraft version AND loader. ModStitch picks the build tool per node.

```
minestratorhelper/
├── settings.gradle.kts        # ModStitch+Loom+MDG+Stonecutter repos; Stonecutter declares <mc>-<loader> nodes
├── stonecutter.gradle.kts     # active node marker + chiseledBuild + allprojects{} repos
├── build.gradle.kts           # CENTRAL script, runs once per node: modstitch{ metadata, loom{}, moddevgradle{} } + deps
├── gradle.properties          # mod_version + gradle (JDK 25)
├── versions/<mc>-<loader>/gradle.properties   # per-node: modstitch.platform + deps.{minecraft,fabric_loader,fabric_api,neoforge,architectury}. The generated build/ + src/ under versions/ is .gitignored
├── src/                       # ░ ALL CODE — every loader, every version ░
│   ├── main/java/fr/minestrator/helper/
│   │   ├── ModEntry.java              # unified entrypoint: //? if fabric { ClientModInitializer } else { @Mod }
│   │   ├── MinestratorHelper.java     # init() entry, called by ModEntry
│   │   ├── api/                       # ApiClient (HTTP/REST) + DTOs
│   │   ├── config/ModConfig.java      # token store; config dir via dev.architectury.platform.Platform
│   │   ├── screen/                    # *Logic (business), ServerStateManager, + Mojmap Screens/Widgets
│   │   └── client/                    # Commands, KeyBinds, ScreenButtons (registered via Architectury events)
│   ├── main/resources/                # lang files, assets/icon
│   └── main/templates/                # fabric.mod.json + META-INF/neoforge.mods.toml (ModStitch fills ${mod_*} tokens)
```

There are **no `fabric/` / `neoforge/` loader modules and no `buildSrc/`** (removed in the ModStitch migration). The loader entrypoints are merged into `ModEntry.java` via `//?`.

Matrix: **1.20.1 = Fabric only** (NeoForge starts at MC 1.20.2); **1.21.1, 1.21.11 and 26.1.2 = Fabric + NeoForge**.

### Architecture pattern

All business logic, API, config, state, AND the GUI (Screens/Widgets in Mojmap) live in `src/` and are loader-agnostic via **Architectury API**:
- Client commands → `ClientCommandRegistrationEvent`
- Keybind → `KeyMappingRegistry` + `ClientTickEvent.CLIENT_POST`
- Buttons injected into vanilla screens → `ClientGuiEvent.INIT_POST`
- Config dir / platform info → `dev.architectury.platform.Platform`
- Logging → SLF4J via `MinestratorHelper.LOGGER`

The only loader-specific source is `ModEntry.java` (entrypoint), bridged with `//? if fabric`. There are **no mixins**. We do **not** use Architectury's `@ExpectPlatform` (that was the only thing tied to Architectury Loom).

## Build & run

The active **node** (in `stonecutter.gradle.kts`, `stonecutter active "<mc>-<loader>"`) is the one Stonecutter preprocesses `src/` for — both the version `//?` and the loader `//?`. Switching nodes rewrites `src/` in place. **Everything must run on JDK 25** (`-Dorg.gradle.java.home=…/jdk-25…`).

```bash
JDK="-Dorg.gradle.java.home=C:/Program Files/Eclipse Adoptium/jdk-25.0.3.9-hotspot"

# Switch the active node (rewrites //? for version AND loader)
./gradlew "Set active project to 1.21.11-fabric" $JDK    # or 1.21.1-neoforge, 1.20.1-fabric, …

# Build one node → versions/<node>/build/libs/minestratorhelper-<loader>-<modver>+<mc>.jar
./gradlew :1.21.11-fabric:build $JDK

# Build EVERY node (all version × loader)
./gradlew chiseledBuild $JDK

# Run a node's dev client (Loom for Fabric, MDG for NeoForge)
./gradlew :1.21.1-neoforge:runClient $JDK
```

No tests exist.

## CI & release

- `.github/workflows/build.yml` — on push to `main` / PRs, runs `chiseledBuild` and uploads `versions/**/build/libs/*.jar`.
- `.github/workflows/release.yml` — on a `v*` tag (or manual dispatch), runs `chiseledBuild` and publishes a **GitHub Release** with the distributable jars (`-sources`/`-dev` excluded).
- Both set up JDK **17 + 21 + 25** (Gradle daemon runs on 25; 17/21 are toolchains for the older lines).
- Cut a release: bump `mod_version` in `gradle.properties`, commit, then `git tag vX.Y.Z && git push origin vX.Y.Z`.

## Handling version & loader differences — Stonecutter `//?`

`src/` is one tree compiled against several MC versions (in Mojmap) and two loaders. Differences are bridged with Stonecutter `//?`. The active block is plain code; the inactive block is `/* */`-wrapped; switching nodes flips them.

**Loader axis** (`constants.match(loader, "fabric", "neoforge")` in `build.gradle.kts`): `//? if fabric { … } else { … }` — used only by `ModEntry.java`.

**Version axis** predicates in use:
- `//? if >=1.21` — `ObjectSelectionList` ctor; `ServerData`/`ConnectScreen.startConnecting` arg shape; `Screen.mouseScrolled` 4-arg.
- `//? if <1.21` — `Screen.renderBackground(GuiGraphics)` explicit; `AbstractSelectionList.getScrollbarPosition()` override.
- `//? if >=1.21.9` — `KeyMapping.Category` (vs `String`); 1.21.11 renames `ResourceLocation` → `net.minecraft.resources.Identifier`.
- `//? if >=1.21.11` — list entries `renderContent(...)` + `getX/getY` (vs 10-arg `render`); mouse/key event objects `mouseClicked(MouseButtonEvent, boolean)`, `mouseDragged`, `mouseReleased`, `keyPressed(KeyEvent)`; `GameProfile` record → `getProfile().name()` (vs `getName()`).

### Non-`//?` rendering gotchas (found in testing)

- **1.21.11 invisible text/fills**: `GuiGraphics.drawString`/`fill` no longer force opaque when a colour's alpha byte is 0. A bare `0xRRGGBB` renders **invisible** on 1.21.11 (was opaque on 1.20.1/1.21.1). Always pass explicit `0xFF` alpha (`0xFFFFFFFF`); OR helper colours with `0xFF000000` at the draw site.
- **1.20.1 widget render order**: `AbstractSelectionList` paints edge gradients over the area outside its bounds, hiding widgets registered before it. Register the list **before** top-bar buttons.

### 26.1 rendering refactor (ported with `//? >=26.1`)

Mojang refactored GUI rendering in 26.1; bridged across the screen classes:
- `GuiGraphics` → `GuiGraphicsExtractor` (import `//?`).
- `Screen.render(GuiGraphics,…)` → `extractRenderState(GuiGraphicsExtractor,…)` (immediate → retained "extract" model); `super.render` → `super.extractRenderState`. Structured as 3 sequential `//?` (signature / `<1.21` renderBackground / super-call) so the body stays common.
- list entries `renderContent(GuiGraphics,…)` → `extractContent(GuiGraphicsExtractor,…)` — a 3-case `//? if >=26.1 … else if >=1.21.11 … else …` (the third case is the old 10-arg `render`).
- `ctx.drawString(...)` → `ctx.text(...)`, centralised in **`Gfx.text(ctx, …)`** so screen bodies don't need per-call `//?` (only the method signatures carrying the `GuiGraphics`/`GuiGraphicsExtractor` type do).
- `Player.displayClientMessage(c, false)` → `sendSystemMessage(c)`, via the **`ChatFeedback.send(player, c)`** helper.
- `fill`, `enableScissor`, `disableScissor`, `pose()` unchanged (called directly on the context).

When porting/adding a feature: write it for the active node, `./gradlew :<node>:build`, then switch active to each other node and add `//?` where the compiler reports mismatches. To find a Mojmap signature for a version, inspect the cached jar:
`javap -classpath ~/.gradle/caches/fabric-loom/<mc>/minecraft-client.jar 'net.minecraft.client.gui.GuiGraphicsExtractor'`

## Minestrator API notes

- Bearer token from `ModConfig`; empty token ⇒ `isConfigured()` false ⇒ calls short-circuit.
- Responses nested under `api.data.*`; `ApiClient` walks them with Gson `JsonObject`. Booleans arrive as integer `0`/`1`.
- Endpoints: `GET /user/`, `GET /user/{id}/servers`, `GET /server/{id}/live`, `GET /server/{id}/console/logs` (ANSI log lines), `PUT /server/{id}/poweraction` (start/stop/restart/kill), `PUT /server/{id}/command`. User id is fetched once and cached.
- All calls return `CompletableFuture` off-thread; marshal UI updates back with `Minecraft.getInstance().execute(...)`.
