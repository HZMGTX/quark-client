<div align="center">

```
 ██████╗ ██╗   ██╗ █████╗ ██████╗ ██╗  ██╗    ██████╗ ██████╗
██╔═══██╗██║   ██║██╔══██╗██╔══██╗██║ ██╔╝   ██╔════╝██╔════╝
██║   ██║██║   ██║███████║██████╔╝█████╔╝    ██║     ██║
██║▄▄ ██║██║   ██║██╔══██║██╔══██╗██╔═██╗    ██║     ██║
╚██████╔╝╚██████╔╝██║  ██║██║  ██║██║  ██╗   ╚██████╗╚██████╗
 ╚══▀▀═╝  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝   ╚═════╝ ╚═════╝
                       quark.cc  ·  v1.0.0
```

<p><i>1000 modules. True XRay. Full ESP suite. Anarchy & Ghost cheating for Minecraft 1.21.1.</i></p>

<p>
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen?style=for-the-badge&logo=minecraft">
  <img alt="Fabric" src="https://img.shields.io/badge/Loader-Fabric-blue?style=for-the-badge">
  <img alt="Modules" src="https://img.shields.io/badge/Modules-1000-red?style=for-the-badge">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk">
  <img alt="Bypasses" src="https://img.shields.io/badge/Bypasses-Grim%20|%20Vulcan%20|%20Polar-purple?style=for-the-badge">
</p>

</div>

---

## What is Quark?

Quark is a feature-complete Minecraft 1.21.1 Fabric utility mod built for competitive play — HVH, anarchy, ghost/closet cheating, and crystal PVP. It ships with exactly **1,000 registered modules** across 7 categories, a custom ClickGUI, an Electron-based desktop launcher with Discord OAuth login, and a full render suite including **true XRay** and **25+ ESP modules**.

---

## Core Advantages

| Feature | Detail |
|---|---|
| **1000 Modules** | Registered and categorised across Combat, Movement, Player, Render, World, Exploit, Misc |
| **True XRay** | Mixin-intercepted block state renderer — non-whitelisted blocks become transparent without texture packs |
| **Full ESP Suite** | 25+ ESP modules: PlayerESP, ChestESP, HoleESP, CrystalESP, ItemESP, MobESP, BlockESP, VoidESP, BeaconESP, GlowESP, LightESP, TargetESP, VehicleESP, NametagESP, ShaderESP, StorageESP, ArmourESP, AnimalESP and more |
| **Fast EventBus** | Zero-reflection MethodHandle bus — runs 50 active modules at once with no FPS drop |
| **Ghost Mode** | RotationManager + randomised delays — bypasses GrimAC, Vulcan, Polar, Matrix on default config |
| **Desktop Launcher** | Electron app with frameless RAIN-style UI, Discord OAuth2 login, process injection scanner, `.exe` installer and portable build |
| **Full Render** | Hardware-accelerated ClickGUI with search, tab filter, smooth animation, category icons, and accent colour theming |
| **Config System** | Named configs with hot-swap — save separate HVH / closet loadouts |

---

## Anti-Cheat Bypasses

Quark's Ghost Mode and movement modules are tested against the following ACs on default/strict configurations:

- **GrimAC** — movement prediction bypass, combat checks, step bypass
- **Vulcan** — full aura and strafe bypasses, inventory move
- **Polar** — packet manipulation, DeSync, Blink
- **Matrix** — HitSelect and AimAssist
- **NCP / AAC** — Reach, KillAura, Step, Scaffold
- **Vanilla Realms / Paper Strict** — closet mode (AimAssist, TriggerBot, AutoClicker)

---

## The 1000 Modules

### ⚔️ Combat (HVH & Closet)
| Module | Description |
|---|---|
| KillAura | Multi-target aura with rotate, WTap, priority filter |
| CrystalAura | Full 1.21.1 crystal rewrite — place/break/predict/switch |
| AnchorAura | Respawn anchor auto-exploit PVP |
| BedAura | Nether/End bed PVP automation |
| TriggerBot | Auto-attack on crosshair with configurable delays |
| AimAssist | Silent smooth aim — horizontal/vertical multipliers |
| HitSelect | Only swing when facing target within tolerance |
| Reach | Extra attack reach (1.0–6.0 blocks) |
| AutoTotem | Keeps totem in off-hand below HP threshold |
| AutoCrystal | Standalone crystal placer/breaker (non-aura mode) |
| Criticals | Forces critical hits via micro-hop or packet |
| WTap | W-tap between attacks to reset sprint |
| Velocity / AntiKnockback | Cancel, reduce, or reverse all knockback |
| AntiVelocity2 | Secondary velocity reducer with per-axis control |
| BowAimbot | Snap-aims at nearest entity with configurable delay |
| BackstabAura | Targets hit from behind for bonus damage |
| AutoArmor | Automatically equips best available armor |
| AutoPot | Auto-throws splash potions when HP falls |
| AutoShield | Raises shield on incoming attacks |
| TargetStrafe | Circles target while fighting |
| FastBow | Releases bow at minimum charge for fast shots |
| *…and 50+ more combat modules* | |

### 🏃 Movement
| Module | Description |
|---|---|
| Speed | Multiple bypass modes (Bhop, Strafe, Friction) |
| Flight | Vanilla / Packet / Creative flight |
| ElytraFly | Packet boost, smooth, strict modes |
| Step | Steps up to 2.5 blocks without jumping |
| Scaffold | Tower, safe, strict with sprint-reset |
| NoFall | Packet-based no-fall damage |
| Sprint | Auto-sprint with omni-directional option |
| Bhop | Strafe-optimised bunny hop |
| LongJump | Extended horizontal jump |
| SafeWalk | No-clip edge detection |
| AntiSlowdown | Removes slowness and soulsand effects |
| IceSpeed | Velocity multiplier on ice blocks |
| AirJump | Jump in mid-air (configurable count) |
| *…and 50+ more movement modules* | |

### 🎨 Render & Visual
| Module | Description |
|---|---|
| **XRay** | **True XRay** via AbstractBlockState mixin — terrain becomes transparent, ores glow; also supports ESP-box overlay mode |
| ESP | Full entity ESP — box/corner/tracer/glow modes, health bar, armor bar, name, distance |
| PlayerESP | Dedicated player boxes with configurable colour |
| ChestESP | All storage blocks (chest, barrel, hopper, shulker, dispenser, furnace) |
| StorageESP | Enhanced storage ESP with category filtering |
| HoleESP | Safe 1×1 crystal holes highlighted in world |
| CrystalESP | End crystal ESP with damage prediction numbers |
| ItemESP | Dropped item boxes with tracers |
| MobESP | Hostile mob ESP |
| AnimalESP | Passive animal ESP |
| VehicleESP | Boat and minecart ESP |
| BeaconESP | Beacon beam highlights |
| VoidESP | Highlights void gaps in terrain |
| GlowESP | Uses Minecraft's own glow outline effect |
| TargetESP | Focused target ring + stat overlay |
| NametagESP | Enhanced nametags with health/gear info |
| LightESP | Highlights dark spots where mobs can spawn |
| ShaderESP | Shader-rendered outlines (no box needed) |
| Nametags | Custom 3D player nametags |
| Tracers | Lines from screen centre to all targets |
| Crosshair | Custom crosshair (Plus/Dot/Circle/T/Arrow) |
| ActiveModules | HUD arraylist with colour modes |
| Fullbright | Gamma override for full visibility underground |
| FOVChanger | Override field of view independently |
| Ambiance | Vignette / particle / glow ambient effects |
| *…and 30+ more render modules* | |

### 🧍 Player
| Module | Description |
|---|---|
| AutoEat | Automatically eats when hunger falls |
| FastEat | Reduces eating time to near-instant |
| NoFall2 | Alternative no-fall via motion cancelling |
| AntiAFK | Rotates and moves to prevent AFK kick |
| AutoFish | Automatic fishing with configurable delay |
| Inventory Manager | Auto-sort, clean, organise inventory |
| AutoTool | Switches to best tool for current block |
| ChestStealer | Loots containers automatically |
| NoHurtCam | Removes screen shake on damage |
| *…and 100+ more player modules* | |

### 🌍 World
| Module | Description |
|---|---|
| Nuker | Breaks blocks in a radius (sphere/flat/column) |
| AutoBuild | Auto-build from schematic |
| HighwayBuilder | Lays floor blocks for nether highways |
| FastPlace | Reduces block place delay to 1 tick |
| Freecam | Detached spectator-mode camera |
| *…and 50+ more world modules* | |

### ⚡ Exploit
| Module | Description |
|---|---|
| Blink | Holds packets then releases — rubber-band teleport |
| DeSync | Sends fake position packets to confuse hitboxes |
| PacketFly | Exploits packet ordering for survival flight |
| PingSpoof | Manipulates reported ping value |
| Timer | Speed-up / slow-down game timer |
| Phase | Vertical/horizontal clip through blocks |
| Disabler | Confuses anti-cheat detection routines |
| AntiCrash | Blocks crash-exploit packets (explosion, particles) |
| *…and 50+ more exploit modules* | |

### ⚙️ Misc
| Module | Description |
|---|---|
| AutoResponse | Auto-replies to chat keywords |
| AutoAd | Sends advertisement messages on interval |
| ChatSpammer | Multi-message chat automation |
| NameProtect | Hides your username from stream viewers |
| GameAlert | Screen flash on low HP/hunger |
| PacketLogger | Logs incoming/outgoing packets to chat |
| *…and 50+ more misc modules* | |

---

## Desktop Launcher

Quark ships with a full **Electron-based desktop launcher** (similar to RAIN Client's launcher):

- **Frameless dark UI** — 860×580, custom titlebar, smooth page transitions
- **Discord OAuth2** — full login flow via system browser + local HTTP callback
- **Home page** — version info, feature highlights, news feed
- **Inject page** — scans for running Java/Minecraft processes, injects the agent jar
- **Changelog** — per-version release notes
- **Settings** — Discord credentials, redirect URI display, launcher preferences
- **Credits** — development team cards
- **Account** — Discord user profile, avatar, logout

### Building the Launcher `.exe`

```bash
cd launcher
npm install
npm run build:win       # produces NSIS installer + portable .exe in dist/
```

### Discord OAuth Setup

1. Create an application at [discord.com/developers/applications](https://discord.com/developers/applications)
2. Under OAuth2 → Redirects, add: `http://localhost:3847/callback`
3. Open the launcher → Settings → enter your **Client ID** and **Client Secret**
4. Click **Continue with Discord** on the login screen

---

## Building the Minecraft Mod

### Requirements

- **Java 21** (mandatory for 1.21.1 — earlier versions will not compile)
- **Fabric Loader 0.16.2+**
- Internet connection for initial Gradle dependency download

### Quick Start

```bash
git clone https://github.com/HZMGTX/quark-client.git
cd quark-client

# Windows
gradlew.bat build

# macOS / Linux
./gradlew build
```

The compiled jar will be at `build/libs/quark-1.21.1-*.jar`.  
Drop it into `.minecraft/mods/` alongside Fabric API and launch.

### Dev Environment (live reload)

```bash
./gradlew runClient
```

This opens Minecraft with the client loaded. The hot-key to open ClickGUI is **Right Shift**.

---

## Code Architecture

```
src/main/java/cc/quark/
├── Quark.java                    # Entry point — initialises all managers
├── event/
│   ├── EventBus.java             # Zero-reflection MethodHandle bus
│   └── events/                   # EventTick, EventRender2D/3D, EventPacketReceive, ...
├── module/
│   ├── Module.java               # Base class — register(), onEnable(), onDisable(), getSuffix()
│   ├── ModuleManager.java        # Registers all 1000 modules + category/class lookup
│   ├── Category.java             # COMBAT, MOVEMENT, PLAYER, RENDER, WORLD, EXPLOIT, MISC
│   └── modules/                  # 1000 module implementations
│       ├── combat/               # ~150 combat modules
│       ├── movement/             # ~150 movement modules
│       ├── player/               # ~200 player modules
│       ├── render/               # ~200 render modules (including XRay, 25 ESP modules)
│       ├── world/                # ~100 world modules
│       ├── exploit/              # ~100 exploit modules
│       └── misc/                 # ~100 misc modules
├── mixin/
│   ├── MixinMinecraft.java       # EventTick dispatch
│   ├── MixinGameRenderer.java    # EventRender3D dispatch + FOV + hurt cam
│   ├── MixinInGameHud.java       # EventRender2D dispatch + crosshair override
│   ├── MixinAbstractBlockState.java  # XRay — makes non-whitelisted blocks transparent
│   ├── MixinWorldRenderer.java   # XRay — triggers chunk reload on enable/disable
│   ├── MixinClientPlayerEntity.java  # Jump, elytra, chat hooks
│   ├── MixinLivingEntity.java    # Damage, knockback hooks
│   ├── MixinClientConnection.java    # Packet send/receive events
│   └── MixinBackgroundRenderer.java  # Fog removal hook
├── gui/
│   ├── ClickGUI.java             # Hardware-accelerated tab GUI with search + animation
│   ├── ThemeManager.java         # Per-theme accent/background colours
│   └── components/CategoryPanel.java
├── setting/                      # BoolSetting, IntSetting, DoubleSetting, ModeSetting, ColorSetting, EnumSetting
├── ghost/
│   ├── RotationManager.java      # Smooth silent rotations for combat modules
│   └── GhostManager.java         # Humanised delay + hitbox management
├── config/ConfigManager.java     # Named config save/load (GSON)
├── command/                      # In-game dot-commands (.bind, .friend, .config, .help)
├── friend/FriendManager.java     # Friend list — excluded from aura/strafe
├── waypoint/WaypointManager.java # Named waypoints with distance display
└── util/                         # RenderUtil, BlockUtil, InventoryUtil, ColorUtil, ChatUtil
```

---

## XRay — Technical Details

Quark's XRay has **three modes**:

| Mode | How it works |
|---|---|
| **True XRay** | `MixinAbstractBlockState` overrides `isOpaque()` and `isSolidBlock()` to return `false` for any block not in the whitelist. This causes the chunk builder to skip those block faces entirely — terrain becomes invisible. Chunk data is reloaded when the module toggles. |
| **ESP Only** | Scans nearby chunks for ore blocks and draws coloured ESP boxes via `EventRender3D`. Night vision + gamma 15× applied. |
| **Both** (default) | True XRay rendering PLUS ESP boxes for easy identification |

### Whitelisted blocks (always visible in True XRay)
Air, Water, Lava, Glass, Ice, Torches, Ladders, Iron Bars, and all enabled ore/storage types.

### Supported ores
Diamond, Gold (inc. Nether), Iron, Emerald, Ancient Debris, Coal, Lapis, Redstone, Copper, Nether Quartz, Chests/Barrels/Shulkers, Monster Spawners.

---

## In-Game Controls

| Action | Default |
|---|---|
| Open ClickGUI | **Right Shift** |
| Right-click module | Set keybind / open settings |
| Search in ClickGUI | Start typing anywhere |
| Command prefix | `.` in chat |

### Client Commands

```
.help                   — List all commands
.bind <module> <key>    — Bind a keybind without opening the GUI
.friend add <name>      — Add a friend (excluded from aura targets)
.friend remove <name>   — Remove a friend
.config save <name>     — Save current module states to a named config
.config load <name>     — Load a named config (hot-swap mid-game)
.config list            — List saved configs
.tp <waypoint>          — Teleport (freecam) to a waypoint
```

---

## Building for Multiple Versions

The project supports Minecraft 1.20.1 through 1.21.5 via Stonecutter. Coordinates are in `build.gradle`:

```bash
./gradlew -Pversion=1.21.4 build   # Build for 1.21.4
./gradlew -Pversion=1.20.1 build   # Build for 1.20.1
```

---

## Disclaimer

This is an open-source educational project. The developers are not responsible for bans, account actions, or violations of server rules. Do not use Quark on servers where cheating is prohibited. Always cheat responsibly — don't ruin casual survival servers. This project is not affiliated with Mojang Studios or Microsoft.
