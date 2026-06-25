<div align="center">

```

  <p><i>The undisputed king of 1.21.1 Fabric clients. Built for HVH, Anarchy, and Ghost/Closet cheating.</i></p>

  <p>
    <img alt="Minecraft Version" src="https://img.shields.io/badge/Minecraft-1.20.x_→_26.2-brightgreen?style=for-the-badge&logo=minecraft">
    <img alt="Platform" src="https://img.shields.io/badge/Platform-Fabric-blue?style=for-the-badge&logo=fabric">
    <img alt="Modules" src="https://img.shields.io/badge/Modules-965+-red?style=for-the-badge">
    <img alt="Java" src="https://img.shields.io/badge/Java-17%2F21-orange?style=for-the-badge&logo=openjdk">
    <img alt="Bypasses" src="https://img.shields.io/badge/Bypasses-Grim|Vulcan|Polar|Matrix-orange?style=for-the-badge">
    <img alt="License" src="https://img.shields.io/badge/License-MIT-lightgrey?style=for-the-badge">
  </p>
</div>

---

## ⚡ What makes Quark different?

Quark isn't just another skidded client. We built this massive 1.21.1 utility mod from scratch to be the fastest, lightest, and most undetectable client you can run — whether you're trying to win a crystal PvP 1v1 or closet cheat on your favorite minigame server.

Most clients force you to choose between anarchy (like Meteor) or ghosting (like Raven). **Quark does both.** We shoved nearly **1000 modules** into a custom-built EventBus that runs with zero tick-lag.

### Core Advantages
- **Zero FPS Drops**: The internal EventBus uses raw `MethodHandle` adapters instead of slow reflection. You won't lag even when packet spoofing and running 50 modules simultaneously.
- **True Ghost Mode**: Our proprietary `GhostManager` handles strict hitboxes, humanized rotation smoothing, and randomized attack delays. It dynamically adjusts your inputs to bypass modern server-side anti-cheats.
- **Hardware-Accelerated ClickGUI**: The menu isn't just functional — it's beautiful. Custom fonts, blur effects, smooth scroll, search bar, tooltips, and dynamic scaling.
- **965+ Modules**: Every category covered. Combat, Movement, Player, Render, World, Exploit, and Misc — each with configurable settings and keybinds.
- **Thread-Safe Architecture**: All packet handling is safely dispatched to the main client thread. No CME crashes, no race conditions.

---

## 🛡️ Supported Anti-Cheats (Bypasses)

Quark's ghost and movement modules are actively tested against the following anti-cheats on default/strict configs:

| Anti-Cheat | Status | Notes |
|---|---|---|
| **GrimAC** | ✅ Bypassed | Movement prediction, combat, and rotation checks |
| **Vulcan** | ✅ Bypassed | Full aura and strafe bypasses |
| **Polar** | ✅ Bypassed | Packet manipulation and DeSync |
| **Matrix** | ✅ Bypassed | HitSelect and AimAssist |
| **NCP** | ⚠️ Partial | Step and velocity on most configs |
| **Vanilla Realms / Paper Strict** | ✅ Bypassed | Ghost-mode safe |

---

## 📦 The Arsenal (965+ Modules)

### ⚔️ Combat (200 modules)
- **Kill Auras**: KillAura, KillAura2, MultiAura, SmartAura, SilentAura, BackstabAura, SpeedAura, MobAura, AxisAura, StabAura
- **Crystal PvP**: CrystalAura2, AutoCrystal, BedAura, AnchorAura, FastCrystal, CrystalBase, CrystalPhase, CrystalTracker, BedCrystal, NetherAnchor
- **Closet PvP**: AimAssist, TriggerBot, HitSelect, AutoClicker, Reach, HitChance, HitboxExpand, AttackRange
- **Auto Utilities**: AutoTotem, AutoArmor, AutoGapple, AutoGapple2, AutoShield, AutoPot, AutoSoup, AutoXP, AutoBow, AutoArrow, AutoCrossbow, AutoSnowball, AutoEnderPearl, AutoPearl, AutoPearl2, AutoEgg, PearlAura, GappleAura, MaceAura, TridentAura
- **Velocity Control**: Velocity, AntiKnockback, AutoVelocity, AntiVelocity2, AntiCrit, VelocityFilter
- **Surround / Trap**: Surround, SurroundBreak, SurroundFill, SurroundPlus, AutoTrap, SelfTrap, Burrow, HoleFiller, HolePush
- **Combat Tools**: WTap, WTap2, JumpReset, Criticals, CriticalHit, CritAura, SmartCrit, SpeedCrit, CritBoost, ManualCrit, QuickHit
- **Advanced**: Backtrack, CombatLag, CombatAssist, CombatPause, FightBot, PearlPredict, SilentBow, SmartBlock, ThrowBot, PotSpam, AutoNet, AntiWither2, AntiCooldown
- **Shields**: ShieldBlock, ShieldBreaker, ShieldPop, ShieldSpoof, QuickShield
- **Misc Combat**: NoHitCooldown, HurtTimer, KeepSprint, SwordSprint, AntiPoison, AntiFireball, AntiFireball2, AntiWeakness, AntiWitch, NoDebuff, DamageFlash, BloodEffect, HitSound, TotemAlert, InstantPop, KillCounter, DeathCounter, ComboExtender, ComboKeeper, FlameAura, EggAura, AntiSurrender, AntiSlap, TrailBurst

### 🏃‍♂️ Movement (201 modules)
- **Speed**: Speed, Bhop, AirStrafe, StrafeBoost, OverclockSprint, GlideSpeed, StrafeSpeed, WaterBoost, UnderwaterSpeed, GroundSpeed, SpeedBoost
- **Flight**: Flight, ElytraFly, AutoElytra, GhostFly, FreeFly, BunnyFly, VerticalFly, XZFly, JetPack, TridentFly, FlyBoost
- **No-Fall / Safety**: NoFall, AntiVoid, AntiVoid2, SafeWalk, AutoSneak, GroundSneak
- **Jump Enhancements**: AirJump, LongJump, HighJump, SlimeJump, VaultJump, RocketJump, NoJumpCooldown
- **Anti-Slow**: AntiSlow, AntiSlow2, NoCobweb, CobwebBypass, NoSlowV, NoFriction, IceSpeed, SoulSandSpeed, SoulWalk, SnowWalk
- **Special Movement**: WallClimb, FastLadder, ClimbSpeed, AirControl, AirControl2, AirBrake, AirDash, AirWalk, AntiWall, Rappel, Orbit
- **Vehicle / Transport**: HorseSpeed, BoatGlide, MinecartBoost, TridentFly
- **Water**: SwimSprint, WaterSpeed2, DolphinSwim
- **Anti-Effects**: AntiLevitation, AntiLag, NoSlowFall, PistonDodge
- **Misc**: StepUp, TeleportStep, YawLock, ZeroVelocity, WindBoost, SandSurfer, KiteStrafe, Skid, SilentStep, ParkourHelper, PearlPhase, AntiKnockback2, AntiKnockback3, ElytraSpeed2, TridentSurf

### 🧍 Player (161 modules)
- **Automation**: AutoEat, SmartEat, AutoFish, AutoFarm, AutoHarvest, AutoHarvest2, AutoSleep, AutoSwim, AutoBed, AutoBed2, AutoRecipe, AutoRefill, AutoNote, AutoNote2, AutoLevelUp, AutoCure, AutoFeed2, AutoTorch, AutoMount, AutoMount2, AutoBoat, AutoLift
- **Anti-Debuff**: AntiBlind, AntiNausea, AntiNausea2, AntiPoison2, AntiEffect, AntiWeakness2, FireProtect
- **Inventory**: ChestSort, ItemSorter, QuickCraft, QuickBank, ArmorSwap, OffhandSwitch, PickupFilter, ThrowFilter, DropProtect, AntiItemDrop, AutoPot2, AutoPot
- **Protection**: AntiAFK, AntiAFK2, AutoEscape, AntiGrief, AntiGrief3, AntiSuffocate, AntiSteal, AntiSlip, AntiTeleport
- **Stealth**: NoSwing, NoCrouchAnim, NoOverlay, HeadRoll, SneakPersist
- **Quality of Life**: HotbarLock, SpawnPoint, WalkSpeed, NightVision, FastPlace, FastPlace2, StepSounds, LavaWalk, ItemTracker, DurabilityAlert, DurabilityHUD, HealthDisplay, SaturationDisplay, LowHealthAlert, ArmorAlert
- **ESP-Player**: CorpseESP, PlayerGlow, HealthTags
- **Misc**: FakeLag2, Freecam, HeadRoll, CustomGravity, AutoElytra2, AutoSwimUp, ArmorAlert2, FireProtect, AntiWeakness2, ItemSorter, AutoCraft3

### 🎨 Render (158 modules)
- **Entity ESP**: PlayerESP, MobESP, ItemESP, ItemESP2, ArmorESP, ArmorStandESP, BeeESP, DragonESP, PetESP, SlimeESP, WitherESP, VehicleESP, CreeperESP, EndermanESP, GhastESP, SpiderESP, WitherSkeletonESP, FishHook, TrailESP
- **Block/World ESP**: ChestESP, ChestESP2, CaveESP, BlockOverlay, BorderESP, EnderChestESP, FurnaceESP, ItemFrameESP, LightLevel, LightESP, LightESP2, PortalESP, SpawnChunkESP, SlimeChunkESP, CrystalTimer, MessageESP, FireworkESP
- **HUD Elements**: ActiveMods, ArmorStatus, AmmoHUD, BiomeOverlay, DurabilityHUD, EnchantHUD, ExperienceHUD, HealthBar, OxygenHUD, RainbowHUD, SaturationHUD, StatHUD, CombatStats, MobCounter, TotemCounter, PopupCounter2, PlayerCount, XPBarHUD, DebugHUD, TimeHUD, VelocityHUD, EntityCount, PingDisplay, Watermark2
- **Targeting**: TargetHUD, Chams, EntityGlow, OverlayESP, ChatESP
- **Advanced**: CustomCrosshair, CornerESP, BossBarESP, FishHook, HeatMap, MapArtHelper, WaypointESP, PlayerModel
- **Anti-Visual**: AntiBlindRender, FullBright, Ambiance
- **Tracers**: Tracers, TNTTracers, ProjectileESP, SkyESP, EntityTracer

### 🌍 World (106 modules)
- **Farming**: AutoFarm, BonemealFarm, TillLand, PlantAll, AutoPlant2, AutoWeed, AutoShear, AutoShear2, AutoStrip, AutoCompost, AutoCompost2
- **Building**: Scaffold, AutoBuild, AutoBridge, AutoBridge2, WorldBridge, AutoPlace, AutoMine, AutoMine2, InstaBreak, BlockBreaker
- **Container Interaction**: ChestStealer, ChestStealer2, ChestHarvest, AutoBarrel, AutoCauldron, AutoDropper, AutoHopper, AutoLectern, AutoSorter, AutoWorkbench, AutoWorkbench2, AutoFurnace, AutoSmelt, ChestOrganizer, AutoCraft2
- **World Utilities**: AutoDoor, AutoSign, BlockFilter, BedBomb, LavaRemover, LiquidFill, WaterPlacer, AntiFireSpread, NetherRoof, PortalTrap, ItemVacuum, ItemMover, TreasureHunter, TreasureHunter2
- **Mob Automation**: AutoBreeder, AutoLeash, AutoSaddle, AutoGrinder, MobKiller, AutoEndRod
- **Misc**: MapArtHelper, InventoryFill, ContainerFill, AutoRemove, AutoFlint, AutoFlint2, AutoCartography, AutoCook, AutoSmelter

### 🐛 Exploit (66 modules)
- **Packet Manipulation**: Blink, AntiCrash, PacketSniffer, PacketFlood, PacketDuplicate, FakeDisconnect, FakeLatency, PositionSpoof, SlotSpoof, NoSlowPacket
- **Anti-Detection**: AntiAim, AntiCheat, AntiDetect, AntiTablist, AntiDesync, EntityDesync, EntitySpoof
- **Bypass**: AntiKick, AntiKick2, AntiKick3, AntiKick4, CubeCraftBypass, MineplexBypass, ChunkBypass, BlockGlitch
- **Fun / Misc**: BookBot, BookCrash, DupExploit, EntitySpammer, EntityTP, InventorySpoof, LagBack, PingSpoofCombat, PortalSpoof, SpeedHack, TabListSpoof, TeleportExploit, TabComplete, SignBypass

### 🔧 Misc (71 modules)
- **Chat Tools**: BetterChat, ChatLogger, ChatTranslator, ChatCleaner, AntiSpam, AntiSpam2, ChatRepeater, ChatHistory, AutoReply, AutoReply2, AutoResponse, AutoResponse2, CommandSpy, VanishSpy, NickSpoof
- **AFK & Session**: AntiAFK, AntiAFK2, AfkDuration, AfkFish, AutoPause, AutoReconnect, AutoReconnect2, AutoSave, SessionTimer
- **HUD / Display**: Watermark, Watermark2, CustomWatermark, PingDisplay, TPSGraph, TimeDisplay, ModuleSearch, ThemeColor, TitleManager, FontManager
- **Notifications**: Notifications, Notifications2, JoinAlert, FriendAlert, KillAlert, DeathAlert, TotemAlert, NotifSound
- **Management**: AltManager, HotkeyManager, ConfigSync, FriendManager, ModuleLog
- **Broadcast**: AutoBroadcast, BroadcastCoords, ItemShare, PortInfo, ServerInfo, ServerLogger, ServerWatch
- **Misc**: CoordsLogger, Macros, Panic, PingChecker, PlayerRadar, PlayerTracker, ScreenCapture, SpellCheck, TabList, CustomPrefix

---

## 🚀 Installation & Building

### Supported Versions

| Minecraft Version | Fabric Loader | Java | Status |
|---|---|---|---|
| 1.20.1 | 0.15.7+ | 17 | ✅ Supported |
| 1.20.4 | 0.15.7+ | 17 | ✅ Supported |
| 1.20.6 | 0.15.11+ | 21 | ✅ Supported |
| 1.21.1 | 0.16.2+ | 21 | ✅ Supported (VCS baseline) |
| 1.21.3 | 0.16.7+ | 21 | ✅ Supported |
| 1.21.4 | 0.16.9+ | 21 | ✅ Supported |
| 1.21.5 | 0.16.11+ | 21 | ✅ Supported |
| 25.1 | 0.16.12+ | 21 | 🔧 Coordinates need verification |
| 25.2 | 0.16.13+ | 21 | 🔧 Coordinates need verification |
| 26.1 | 0.17.0+ | 21 | 🔧 Coordinates need verification |
| 26.2 | 0.17.1+ | 21 | 🔧 Coordinates need verification |

> For 25.x and 26.x (year-based Mojang naming): verify exact yarn/loader/fabric-api
> coordinates at [fabricmc.net/develop](https://fabricmc.net/develop) and update `build.gradle` before building.

### Building from source

```bash
# Clone the repository
git clone https://github.com/HZMGTX/Minecraft-Hack-Client.git
cd Minecraft-Hack-Client

# Build the active version (defaults to 1.21.1)
./gradlew build           # Linux/macOS
gradlew.bat build         # Windows

# Switch active version (IDE / runClient target)
./gradlew "Set active project to 1.20.1"
./gradlew "Set active project to 26.2"

# Build ALL versions at once
./gradlew chiseledBuild
```

The compiled `.jar` for each version lands in `build/libs/`. Drop it alongside Fabric API into `.minecraft/mods/`.

### Dev setup

```bash
# Run with the currently active version
./gradlew runClient

# Switch version and run
./gradlew "Set active project to 1.21.5" && ./gradlew runClient
```

---

## 🎮 Usage

| Action | How |
|---|---|
| Open ClickGUI | `Right Shift` |
| Set keybind | Right-click any module in the GUI |
| Client commands | Type `.` in chat |
| Save config | `.config save <name>` |
| Load config | `.config load <name>` |
| Friend list | `.friend add <name>` |
| Quick bind | `.bind <module> <key>` |
| Help | `.help` |

---

## In-Game Controls

```
cc.quark
├── event/          MethodHandle-based EventBus (EventTick, EventPacketReceive, EventChat, ...)
├── mixin/          Minecraft injection points (ClientPlayer, ClientConnection, GameRenderer, ...)
├── module/
│   ├── Module.java         Base class — register settings, handle enable/disable
│   ├── ModuleManager.java  Registers all 965+ modules at startup
│   ├── Category.java       COMBAT, MOVEMENT, PLAYER, RENDER, WORLD, EXPLOIT, MISC
│   └── modules/
│       ├── combat/         200 modules
│       ├── movement/       201 modules
│       ├── player/         161 modules
│       ├── render/         158 modules
│       ├── world/          106 modules
│       ├── exploit/         66 modules
│       └── misc/            71 modules
├── setting/        BoolSetting, IntSetting, DoubleSetting, ModeSetting, ColorSetting, StringSetting
├── gui/            ClickGUI panels, category tabs, search, blur, smooth scroll
├── ghost/          RotationManager, GhostManager (humanized anti-cheat bypass)
└── util/           ChatUtil, TimerUtil, RenderUtil, MathUtil, EntityUtil
```

### Adding a module

```java
package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class MyModule extends Module {

    private final BoolSetting option = register(new BoolSetting("Option", "Does a thing", true));

    public MyModule() {
        super("MyModule", "Short description here", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!option.isEnabled()) return;
        // your logic here
    }
}
```

Then add `register(new MyModule());` in `ModuleManager.java` under the appropriate section header.

---

## Building for Multiple Versions

This is an open-source project for educational and research purposes. We are not responsible for bans, account terminations, or violations of any server's terms of service. Use responsibly. Don't ruin vanilla servers for other players — go play on anarchy.
