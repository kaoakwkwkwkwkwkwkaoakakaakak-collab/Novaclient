# NovaClient

NovaClient is a **client-side-only** performance console for Minecraft **1.21.1**. It is built for Fabric and keeps the renderer boundary clean: Sodium is optional, detected when present, and never patched or replaced by Nova.

## What is included

- A focused in-game console opened with **Right Shift**.
- Four tabs: **Overview**, **Modules**, **Profiles**, and **Settings**.
- **36** small, reversible optimization modules across Performance, Render, World, and System.
- Search and category filtering in the module library.
- Competitive, Balanced, and Cinematic profiles.
- Local FPS/module HUD, disabled by default until Debug Overlay is enabled.
- Safe vanilla option hooks for Dynamic FPS, Entity Shadows, Render Distance Budget, and Simulation Budget. Vanilla values are restored when those modules are turned off.
- Sodium-aware status reporting with no hard Sodium dependency and no renderer mixins.
- A local properties config at `config/novaclient.properties`; Nova makes no network requests and collects no telemetry.

> **Compatibility note:** “Compatible with every mod” cannot be guaranteed by any client because mods can conflict with one another. Nova intentionally avoids invasive mixins and hard renderer dependencies. It is designed to coexist with Fabric API, Sodium, Iris, and other client mods. Modules that would need renderer-specific hooks are safe switches in this base release and do not pretend to change server simulation.

## Supported environment

| Component | Requirement |
| --- | --- |
| Minecraft | 1.21.1 only |
| Loader | Fabric Loader 0.16.10 or newer |
| API | Fabric API 0.116.0+1.21.1 or newer |
| Java | 21 or newer |
| Renderer | Vanilla or Sodium 0.6.x+ (optional) |

## Build

```bash
./gradlew build
```

The remapped jar is written to `build/libs/novaclient-1.0.0.jar`.

For a development client:

```bash
./gradlew runClient
```

## Install

1. Install Fabric Loader 0.16.10+ for Minecraft 1.21.1.
2. Add Fabric API for 1.21.1.
3. Put the NovaClient jar in the instance `mods` directory.
4. Add Sodium for 1.21.1 if desired; it is recommended, not required.
5. Launch and press **Right Shift**.

## Project layout

- `src/main/java/com/novaclient/module` — module registry, profiles, persistence, and safe option hooks.
- `src/main/java/com/novaclient/ui` — the four-tab Minecraft UI and local HUD.
- `src/main/java/com/novaclient/compat` — optional mod discovery only.
- `src/main/resources/fabric.mod.json` — client-only Fabric metadata and version constraints.

## License

NovaClient code is licensed under the **Apache License 2.0**. This is a software license with an explicit patent grant and is a safer, more appropriate choice for a Fabric mod than CC BY-ND: Creative Commons recommends its licenses for creative works, not software, and the ND restriction would prevent ordinary software forks and fixes. See [`LICENSE`](LICENSE).

Minecraft, Fabric, Sodium, and other third-party projects remain under their own licenses. NovaClient does not bundle them.
