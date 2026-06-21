# Combat Lab

Combat Lab is the beginning of a transparent Minecraft PvP telemetry and training client. The first slice keeps Minecraft access behind a small bridge and exposes stable combat state for future HUD, replay, and coaching features.

## Current target

- Minecraft 26.2
- Fabric Loader 0.19.3
- Fabric API 0.152.2+26.2
- Java 25

## In game

Combat Lab starts with no HUD elements visible. Press `Right Shift` (rebindable under Minecraft Controls) to open the HUD editor. Drag module previews to reposition them, or use the centered **HUD Options** button to enable and disable modules. Options and positions are saved to `config/combatlab.json`.

Available modules currently include FPS, rolling one-second left-click CPS, and movement status. Movement status appears only while crouching or sprinting and distinguishes held sprint from Minecraft's toggle-sprint mode. All modules are disabled by default.

Compact text modules render without background boxes and use Minecraft's native text shadow for contrast.

The options menu is divided into General and HUD submenus. General contains Fullbright, debug logging, and achievement-notification suppression; HUD contains module toggles. Fullbright modifies Minecraft's renderer-neutral lightmap state and does not add a visible potion effect. Achievement suppression hides client-side advancement toasts without changing server-side progress.

The HUD editor snaps nearby modules together and also aligns their left, right, center, top, bottom, or vertical center axes across the screen. Editor-outline pixels are hidden wherever they touch or overlap another HUD module, including perpendicular corner extensions.

HUD features implement a shared module contract and are registered centrally, so future modules automatically participate in rendering, enablement, editor previews, hit-testing, and dragging.

HUD positions are stored as normalized coordinates, preserving relative placement across window sizes and GUI scales. Legacy flat configuration files are migrated automatically to the versioned module schema.

Enable **Debug logging** under HUD Options to write meaningful client activity—configuration changes, editor navigation, target transitions, and attack samples—to Minecraft's `latest.log`. Per-frame rendering is intentionally not logged.

## Build

```powershell
.\gradlew.bat build
```

The development client can later be launched with:

```powershell
.\gradlew.bat runClient
```

## First milestones

- [x] Client-only Fabric project
- [x] Version-sensitive Minecraft bridge
- [x] Attack strength, target, distance, and ping state
- [x] Minimal in-game HUD
- [x] Bounded attack event history (target, miss, distance, cooldown, ping, and game tick)
- [x] JSON configuration and in-game HUD options
- [x] Versioned configuration migration and atomic saves
- [x] Resolution-independent HUD layout
- [x] Cached HUD presentation model and rate-aware telemetry
- [x] Unit tests for layout, history, and configuration migration

## Renderer compatibility

Combat Lab renders through Fabric's `HudElement` and Minecraft's `GuiGraphicsExtractor`. It does not call OpenGL or Vulkan directly, so Minecraft owns the active graphics backend. The `check` task uses Java 25's Class-File API to inspect compiled bytecode and fails on direct OpenGL, Vulkan, or Mojang OpenGL implementation references.
