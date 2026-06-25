# Building Quark Client

## Requirements
- Java 21+ (Java 17 is required only when building the 1.18.2–1.20.4 targets)
- Gradle 8.x (wrapper included — use `./gradlew`)
- Internet connection (to download Minecraft assets and Fabric API)

## Quick Build (1.21.1)
```bash
./gradlew build
```
Output: `versions/1.21.1/build/libs/quark-*.jar`

## Build a Specific Version
```bash
./gradlew "1.21.4:build"
./gradlew "1.20.1:build"
./gradlew "1.18.2:build"
```

## Build All Versions
```bash
./gradlew chiseledBuild
```
Stonecutter will compile every version listed in `settings.gradle` and place each JAR under `versions/<ver>/build/libs/`.

## Switch the Active Version (IDE / IntelliJ)
```bash
./gradlew "Set active project to 1.20.4"
```
This updates `stonecutter.gradle` so that your IDE's project model targets the selected version.

## Supported Minecraft Versions
| Version | Status | Notes |
|---------|--------|-------|
| 1.21.4 | Supported | |
| 1.21.1 | Supported | Primary development target |
| 1.20.6 | Supported | |
| 1.20.4 | Supported | |
| 1.20.1 | Supported | |
| 1.19.4 | Supported | |
| 1.18.2 | Supported | Java 17 required |
| 1.17.1 | Not supported | Would need Java 16 + older Loom |
| 1.16.5 | Not supported | Would require significant refactor |
| 1.12.2 | Not supported | Requires Forge port |
| 1.8.9  | Not supported | Requires Legacy Fabric or Forge port |

## Installing
1. Build the JAR for your Minecraft version (see above).
2. Copy the JAR from `versions/<ver>/build/libs/quark-*.jar` into your Minecraft mods folder:
   - Windows: `%APPDATA%\.minecraft\mods\`
   - macOS: `~/Library/Application Support/minecraft/mods/`
   - Linux: `~/.minecraft/mods/`
3. Launch Minecraft with Fabric Loader installed.
4. Press **Insert** to open the Quark GUI in-game.

## Version-Specific Dependency Matrix
Dependency coordinates are maintained in `build.gradle` under the `matrix` map.
Check [fabricmc.net/develop](https://fabricmc.net/develop) when adding a new Minecraft version.

## Compat Layer
Cross-version API shims live in `src/main/java/cc/quark/compat/`:
- `MCVersion.java` — runtime version detection helpers
- `PlayerCompat.java` — player API wrappers
- `ToolMaterialCompat.java` — tool material accessor wrappers

When an upstream Minecraft or Fabric API change breaks a call site, add or update the appropriate compat class instead of scattering version checks throughout the codebase.
