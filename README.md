<div align="center">
  <h1>⚛️ Quark.cc</h1>
  <p><i>The best open-source 1.21.1 Fabric client. Built for anarchy, ghost cheating, and everything in between.</i></p>

  <p>
    <img alt="Minecraft Version" src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen?style=for-the-badge&logo=minecraft">
    <img alt="Platform" src="https://img.shields.io/badge/Platform-Fabric-blue?style=for-the-badge&logo=fabric">
    <img alt="Modules" src="https://img.shields.io/badge/Modules-300+-red?style=for-the-badge">
  </p>
</div>

---

## What is Quark?
Quark is a massive 1.21.1 utility mod / client written from scratch to be fast, lightweight, and completely undetectable if configured right. 

Most clients either focus purely on anarchy (CrystalAura) or purely on ghost cheating (AimAssist). Quark does both. We have over 300 modules packed into a custom EventBus that runs with zero tick-lag. No more dropping frames when someone dumps packets in your chunk.

### Why use Quark?
- **Zero FPS Drops**: The internal EventBus uses raw `MethodHandle` adapters instead of slow reflection. You won't lag even with 50 modules toggled.
- **Modern ClickGUI**: Looks clean, scrolls smooth, has tooltips, search bar, and custom color presets. 
- **True Ghost Mode**: Our GhostManager forces strict hitboxes, humanized rotation smoothing, and attack delays to bypass modern server-side anti-cheats (Grim, Polar, Vulcan, etc).
- **Stonecutter Ready**: Easily ports between versions. Currently fully updated and mapped for 1.21.1.

---

## 📦 Modules (Just the good stuff)

We have over 300 modules. Here are the ones you actually care about:

### ⚔️ Combat
- **Auras**: KillAura, CrystalAura (rewritten for 1.21.1 placements), AnchorAura, BedAura
- **Ghost**: AimAssist, TriggerBot, HitSelect, AutoClicker (with randomization)
- **Utility**: AutoTotem (zero delay), AutoArmor, TargetStrafe, BowAimbot, BackstabAura

### 🏃‍♂️ Movement
- **Bypasses**: Step, Velocity (packet-based), LongJump, SafeWalk
- **Flight**: ElytraFly (packet/boost modes), Flight, JetPack, Hover
- **Misc**: AirJump, Bhop, Spider, FastFall, SprintReset

### 🌍 World & Player
- **Automation**: Scaffold, AutoBuild, ChestStealer, AutoTool, InventoryManager
- **Griefing**: Nuker, FastPlace, Freecam, XCarry

### 🐛 Exploit
- **Network**: PingSpoof, Disabler, PacketFly, DeSync, AntiCrash
- **Misc**: Phase, HighwayBuilder, ChatSpammer

### 🎨 Render
- **ESP**: PlayerESP, ChestESP, Tracers, Chams (with custom colors/glow)
- **HUD**: ActiveModules, ArmorStatus, TargetHUD, CustomFOV, Fullbright, BlockHighlight

---

## 🚀 How to Install & Build

### Requirements
- **Java 21** (Minecraft 1.21.1 requires it)
- **Fabric Loader 0.16.2+**

### Building it yourself

1. Clone the repo:
   ```bash
   git clone https://github.com/YourUsername/Minecraft-Hack-Client.git
   cd Minecraft-Hack-Client
   ```

2. Compile the jar:
   - On Windows: `gradlew.bat build`
   - On Mac/Linux: `./gradlew build`

3. Grab the compiled jar from `build/libs/` and throw it into your `.minecraft/mods` folder.

### Dev Setup
If you want to edit the code and test it live, just run:
```bash
gradlew.bat runClient
```

---

## 🎮 How to Use

- **ClickGUI**: Press `Right Shift` to open the menu.
- **Keybinds**: Right-click any module in the GUI to set a custom bind.
- **Commands**: Type `.` in chat to use client commands.
  - `.bind [module] [key]`
  - `.friend add [name]` (friends won't be targeted by auras)
  - `.config save/load` (saves your current setup)

---

## 💻 Code Architecture

If you're looking to fork this or contribute, here's how the codebase works:

- `cc.quark.event`: The core `MethodHandle` EventBus. It's fast, don't break it.
- `cc.quark.mixin`: Where we inject into Minecraft's actual code (`MixinMinecraft`, `MixinClientConnection`, etc).
- `cc.quark.module`: All 300+ modules live here, split by category.
- `cc.quark.gui`: The ClickGUI rendering logic.
- `cc.quark.ghost`: The rotation and delay managers for bypassing anti-cheats.

---

## ⚠️ Disclaimer

This is an open-source project meant for educational purposes. We aren't responsible if you get banned on your favorite server, rat yourself with a bad fork, or break terms of service. Use it responsibly and don't ruin the game for people in vanilla survival servers.
