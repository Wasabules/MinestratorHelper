# Minestrator Helper

A **client-side Minecraft mod** that brings your [Minestrator](https://minestrator.com/)-hosted servers directly into the game: browse your servers, start / stop / join them, open an in-game console, and control power actions — without leaving Minecraft.

Built from a **single codebase** that targets multiple mod loaders **and** multiple Minecraft versions.

## Supported versions

| Minecraft | Fabric | NeoForge |
|-----------|:------:|:--------:|
| 1.20.1    | ✅     | —        |
| 1.21.1    | ✅     | ✅       |
| 1.21.11   | ✅     | ✅       |
| 26.1.2    | ✅     | ✅       |

> NeoForge does not exist for 1.20.1 (it starts at MC 1.20.2), so 1.20.1 is Fabric-only.

## Features

- **My Servers** screen (a button added to the multiplayer menu): list your boxes & servers, see live status (online / starting / stopping / offline), **live monitoring gauges** (CPU / RAM / disk / players), start/stop, and join in one click.
- **Live in-game console** (default key: **F6**): streams the server console in real time with ANSI colours and word-wrap, mouse-wheel + **draggable scrollbar** (the view freezes while you scroll so new lines don't shift it), a "back to bottom" button, and clickable **INFO / WARN / ERROR filter pills**. Send commands from the input at the bottom.
- **Client commands**: `/reboot`, `/mstop`, `/mstart`, and **`/sudo <command>`** — run any console command from chat, with **tab-completion** of popular commands (`op`, `kick`, `ban`, `gamemode`, …), online players, and gamemodes (e.g. `/sudo op Notch`).
- **Power buttons** in the pause menu (restart / stop the targeted server).
- **Server pinning (proxy-safe)** — choose which hosted server the console, overlay, pause buttons **and** chat commands act on, via **My Servers → "Pin F6"**. Essential behind a Bungee/Velocity proxy, where the connected IP is the proxy and not the backend, so auto-detection can't tell which server you're really on.
- **In-game stats overlay** (keybind, **F7** by default, rebindable): toggle a compact CPU / RAM / players overlay for the targeted server without opening the console.
- **"My Servers" from the pause menu** (always available), plus a shortcut to it in the top-right of the console screen.

## Download

Pick the jar for **your Minecraft version and loader** — these links always serve the newest release:

| Minecraft | Fabric | NeoForge |
|-----------|--------|----------|
| 1.20.1  | [⬇ download](https://github.com/Wasabules/MinestratorHelper/releases/latest/download/minestratorhelper-fabric-1.20.1.jar) | — |
| 1.21.1  | [⬇ download](https://github.com/Wasabules/MinestratorHelper/releases/latest/download/minestratorhelper-fabric-1.21.1.jar) | [⬇ download](https://github.com/Wasabules/MinestratorHelper/releases/latest/download/minestratorhelper-neoforge-1.21.1.jar) |
| 1.21.11 | [⬇ download](https://github.com/Wasabules/MinestratorHelper/releases/latest/download/minestratorhelper-fabric-1.21.11.jar) | [⬇ download](https://github.com/Wasabules/MinestratorHelper/releases/latest/download/minestratorhelper-neoforge-1.21.11.jar) |
| 26.1.2  | [⬇ download](https://github.com/Wasabules/MinestratorHelper/releases/latest/download/minestratorhelper-fabric-26.1.2.jar) | [⬇ download](https://github.com/Wasabules/MinestratorHelper/releases/latest/download/minestratorhelper-neoforge-26.1.2.jar) |

Or browse every file on the [Releases](../../releases) page. **You also need the dependencies** — see Setup just below.

## Setup

1. Install your loader:
   - **Fabric** → [Fabric Loader](https://fabricmc.net/) **and** [Fabric API](https://modrinth.com/mod/fabric-api)
   - **NeoForge** → [NeoForge](https://neoforged.net/)
2. Install **[Architectury API](https://modrinth.com/mod/architectury-api)** — **required on both loaders** (the mod won't load without it; pick the build matching your MC version + loader).
3. Drop the matching mod jar (from **Download** above) into your `mods/` folder.
4. In game, open **My Servers → Config** and paste your Minestrator API token.

## Building from source

Requires **JDK 25** (the Gradle daemon runs on it; JDK 17 & 21 are also used as toolchains for the older lines). The project uses [Stonecutter](https://stonecutter.kikugie.dev/) for multi-version × multi-loader and [ModStitch](https://github.com/isXander/modstitch) to drive the official Fabric Loom + NeoForge ModDevGradle toolchains; [Architectury API](https://docs.architectury.dev/) is kept as a runtime library.

```bash
# Build every version × loader → versions/<mc>-<loader>/build/libs/
./gradlew chiseledBuild
```

To work on a specific node (`<mc>-<loader>`):

```bash
./gradlew "Set active project to 1.21.11-fabric"
./gradlew :1.21.11-fabric:runClient        # or :1.21.1-neoforge:runClient
```

> All Gradle commands must run on JDK 25, e.g. `-Dorg.gradle.java.home=…/jdk-25…`.

See [CLAUDE.md](CLAUDE.md) for the full architecture, toolchain, and how version differences are handled.

## Releases

Releases are built and published by GitHub Actions. To cut one: bump `mod_version` in `gradle.properties`, commit, then push a tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

This runs `chiseledBuild` and attaches all 7 version × loader jars to a new GitHub Release. You can also trigger it manually from the **Actions → Release** tab.

## License

All Rights Reserved.
