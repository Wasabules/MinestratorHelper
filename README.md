# Minestrator Helper

A **client-side Minecraft mod** that brings your [Minestrator](https://minestrator.com/)-hosted servers directly into the game: browse your servers, start / stop / join them, open an in-game console, and control power actions — without leaving Minecraft.

Built from a **single codebase** that targets multiple mod loaders **and** multiple Minecraft versions.

## Supported versions

| Minecraft | Fabric | NeoForge |
|-----------|:------:|:--------:|
| 1.20.1    | ✅     | —        |
| 1.21.1    | ✅     | ✅       |
| 1.21.11   | ✅     | ✅       |

> NeoForge does not exist for 1.20.1 (it starts at MC 1.20.2), so 1.20.1 is Fabric-only.

## Features

- **My Servers** screen (a button added to the multiplayer menu): list your boxes & servers, see live status (online / starting / stopping / offline), start/stop, and join in one click.
- **In-game console** (default key: **F6**) to send commands to the server you're connected to.
- **Client commands**: `/reboot`, `/mstop`, `/mstart`.
- **Power buttons** added to the pause menu while connected to a hosted server.

## Setup

1. Install [Fabric Loader](https://fabricmc.net/) (+ [Fabric API](https://modrinth.com/mod/fabric-api)) **or** [NeoForge](https://neoforged.net/), and [Architectury API](https://modrinth.com/mod/architectury-api).
2. Drop the matching jar into your `mods/` folder.
3. In game, open **My Servers → Config** and paste your Minestrator API Bearer token.

## Building from source

Requires **JDK 21**. The project uses [Stonecutter](https://stonecutter.kikugie.dev/) for multi-version and [Architectury](https://docs.architectury.dev/) for multi-loader.

```bash
./gradlew chiseledBuild        # build every version × loader → build/libs/<version>/<loader>/
```

To work on a specific version:

```bash
./gradlew "Set active project to 1.21.1"   # 1.20.1 | 1.21.1 | 1.21.11
./gradlew runActiveClientFabric            # or runActiveClientNeoforge
```

See [CLAUDE.md](CLAUDE.md) for the full architecture, toolchain, and how version differences are handled.

## License

All Rights Reserved.
