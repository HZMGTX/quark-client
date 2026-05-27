<div align="center">

```text
  ____                  _                 
 / __ \                | |                
| |  | |_   _  __ _ _ __| | __  ___ ___  
| |  | | | | |/ _` | '__| |/ / / __/ __| 
| |__| | |_| | (_| | |  |   < | (_| (__  
 \___\_\\__,_|\__,_|_|  |_|\_(_)___\___| 
```
  <p><i>The undisputed king of 1.21.1 Fabric clients. Built for HVH, Anarchy, and Ghost/Closet cheating.</i></p>

  <p>
    <img alt="Minecraft Version" src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen?style=for-the-badge&logo=minecraft">
    <img alt="Platform" src="https://img.shields.io/badge/Platform-Fabric-blue?style=for-the-badge&logo=fabric">
    <img alt="Modules" src="https://img.shields.io/badge/Modules-300+-red?style=for-the-badge">
    <img alt="Bypasses" src="https://img.shields.io/badge/Bypasses-Grim|Vulcan|Polar-orange?style=for-the-badge">
  </p>
</div>

---

## ⚡ What makes Quark different?
Quark isn't just another skidded client. We built this massive 1.21.1 utility mod from scratch to be the fastest, lightest, and most undetectable client you can run, whether you're trying to win a crystal PVP 1v1 or closet cheat on your favorite minigame server. 

Most clients force you to choose between anarchy (like Meteor) or ghosting (like Raven). **Quark does both.** We shoved over 300 modules into a custom-built EventBus that runs with zero tick-lag. 

### Core Advantages
- **Zero FPS Drops**: The internal EventBus uses raw `MethodHandle` adapters instead of slow reflection. You won't lag even when packet spoofing and running 50 modules at once.
- **True Ghost Mode**: Our proprietary `GhostManager` handles strict hitboxes, humanized rotation smoothing, and randomized attack delays. It dynamically adjusts your clicks to bypass modern server-side anti-cheats.
- **Hardware-Accelerated ClickGUI**: The menu isn't just functional, it's beautiful. Custom fonts, blur effects, smooth scroll, search, tooltips, and dynamic scaling.
- **Stonecutter Ready**: Easily ports between versions. Mappings are fully optimized for native 1.21.1 memory structures.

---

## 🛡️ Supported Anti-Cheats (Bypasses)
Quark's ghost and movement modules are actively tested against the following anti-cheats on default/strict configs:
- **GrimAC** (Bypasses movement, prediction, and combat checks with Ghost Mode enabled)
- **Vulcan** (Full aura and strafe bypasses)
- **Polar** (Packet manipulation and DeSync works flawlessly)
- **Matrix** (HitSelect and AimAssist bypasses)
- *Vanilla Realms / Paper Strict* 

---

## 📦 The Arsenal (300+ Modules)

We literally have over 300 modules. Here are the highlights you actually need to know about:

### ⚔️ Combat (HVH & Closet)
- **Auras**: KillAura, CrystalAura (rewritten for 1.21.1 block placements), AnchorAura, BedAura, BackstabAura
- **Closet**: AimAssist, TriggerBot, HitSelect, AutoClicker (with randomized CPS curves), Reach
- **Utility**: AutoTotem (zero delay, strict mode), AutoArmor, TargetStrafe, BowAimbot, Criticals, WTap

### 🏃‍♂️ Movement
- **Bypasses**: Step (NCP/Vanilla), Velocity (packet/cancel modes), LongJump, SafeWalk, AntiKnockback
- **Flight**: ElytraFly (packet/boost/strict modes), Flight, JetPack, Hover, Glide
- **Misc**: AirJump, Bhop (strafe/jump modes), Spider, FastFall, SprintReset, IceSpeed

### 🌍 World & Automation
- **Builders**: Scaffold (tower/strict/safe modes), AutoBuild, HighwayBuilder
- **Automation**: ChestStealer (with delay), AutoTool, InventoryManager, AutoFarm, AutoFish
- **Griefing**: Nuker, FastPlace, Freecam, XCarry, BlockHighlight

### 🐛 Exploit & Network
- **Packet Magic**: PingSpoof, Disabler, PacketFly, DeSync, AntiCrash, Blink
- **Misc**: Phase (vclip/hclip), ChatSpammer, Timer, NoPacketKick

### 🎨 Render & Visuals
- **ESP**: PlayerESP (2D/3D), ChestESP, ItemESP, Tracers, Chams (with custom colors/glow/xray)
- **HUD**: ActiveModules, ArmorStatus, TargetHUD, CustomFOV, Fullbright, NameProtect (hides names on stream)

---

## 🚀 How to Install & Build

### Requirements
- **Java 21** (Minecraft 1.21.1 will not run without this)
- **Fabric Loader 0.16.2+**

### Building it yourself (The right way)

1. Clone the repo to your local machine:
   ```bash
   git clone https://github.com/YourUsername/Minecraft-Hack-Client.git
   cd Minecraft-Hack-Client
   ```

2. Compile the `.jar`:
   - On Windows: `gradlew.bat build`
   - On Mac/Linux: `./gradlew build`

3. Grab the compiled jar from `build/libs/` and throw it into your `.minecraft/mods` folder. Launch the game.

### Dev Setup (For coders)
If you want to edit the code and test it live, just run:
```bash
gradlew.bat runClient
```

---

## 🎮 Pro Tips & How to Use

- **ClickGUI**: Press `Right Shift` to open the menu. 
- **Keybinds**: Right-click ANY module in the GUI to set a custom bind. You can also bind modules using chat.
- **Commands**: Type `.` in chat to use client commands.
  - `.help` - See everything
  - `.bind [module] [key]` - Quick bind
  - `.friend add [name]` - Friends won't be targeted by Auras or TargetStrafe
  - `.config save [name]` - Save your HVH or Closet configs separately
  - `.config load [name]` - Hot-swap configs mid-game

---

## 💻 Code Architecture

If you're looking to fork this, contribute, or skid some code (we see you), here's how the codebase works:

- `cc.quark.event`: The core `MethodHandle` EventBus. It's ridiculously fast. Don't break it.
- `cc.quark.mixin`: Where we inject into Minecraft's actual code (`MixinMinecraft`, `MixinClientConnection`, `MixinClientPlayerEntity`).
- `cc.quark.module`: All 300+ modules live here, split cleanly by category.
- `cc.quark.gui`: The ClickGUI rendering logic, CategoryPanels, and Settings.
- `cc.quark.ghost`: The rotation (`RotationManager`) and delay managers for bypassing modern anti-cheats.

---

## ⚠️ Disclaimer

This is an open-source project meant for educational purposes. We aren't responsible if you get banned on your favorite server, rat yourself with a bad fork, or break terms of service. Use it responsibly and don't ruin the game for people in vanilla survival servers. Go play on anarchy.
