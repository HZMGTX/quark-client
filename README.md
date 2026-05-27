<div align="center">
  <h1>⚛️ Quark.cc | The Ultimate Fabric Client</h1>
  <p><i>Unleash the full potential of your Minecraft experience with the most advanced, high-performance, and feature-rich utility mod built for 1.21.1.</i></p>

  <p>
    <img alt="Minecraft Version" src="https://img.shields.io/badge/Minecraft-1.21.1-brightgreen?style=for-the-badge&logo=minecraft">
    <img alt="Platform" src="https://img.shields.io/badge/Platform-Fabric-blue?style=for-the-badge&logo=fabric">
    <img alt="Java Version" src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java">
    <img alt="License" src="https://img.shields.io/badge/License-MIT-purple?style=for-the-badge">
    <img alt="Modules" src="https://img.shields.io/badge/Modules-300+-red?style=for-the-badge">
    <img alt="Status" src="https://img.shields.io/badge/Status-Active_Development-success?style=for-the-badge">
  </p>
</div>

---

## 🌟 Introduction

**Quark.cc** is a bleeding-edge, high-performance utility mod (hack client) for Minecraft **1.21.1** Fabric. Built from the ground up to dominate both anarchy servers and strict anti-cheat environments, Quark provides an unparalleled suite of features. Whether you need silent ghost cheating, crystal aura domination, or massive world-editing exploits, Quark has you covered.

Our architecture is designed around a zero-lag EventBus and highly optimized Mixins, ensuring maximum FPS while manipulating the Minecraft network protocol and rendering pipelines at a fundamental level.

---

## ✨ Unrivaled Features

### ⚡ Performance & Core
- **Blazing Fast EventBus:** Quark uses an ultra-optimized `MethodHandle` event system that eliminates tick-lag and reflection overhead. Processes thousands of packets per second with absolutely zero FPS drops.
- **1.21.1 Native Architecture:** Full, native support for the latest Minecraft rendering engines, network packet structures, and obfuscation mappings.
- **Stonecutter Multi-Version Support:** Effortlessly adaptable codebase engineered for long-term multi-version scalability.

### 🎨 Stunning Visuals & UI
- **Hardware-Accelerated ClickGUI:** A sleek, fully customizable, modern GUI with smooth animations, scrolling, dynamic tooltips, and search capabilities.
- **Premium Aesthetics:** Full color customization, custom fonts, blur effects, and dynamic category panel interactions.
- **In-Game HUD:** Fully customizable array lists, armor status, watermark, active modules, and target HUDs.

### 👻 Ghost Mode & Anti-Cheat Bypasses
- **Humanized Rotations:** Mathematically smoothed rotation locking to bypass strict look-heuristics on modern anti-cheats (e.g., Grim, Polar, Vulcan).
- **Latency Spoofing & DeSync:** Intentionally manipulate your ping and packet flow to abuse server-side movement predictions.
- **Discrete Event Handling:** Silent aim, strict hitboxes, and customizable delays for maximum stealth.

---

## 📦 The Arsenal (300+ Modules)

With over 300 highly configurable modules, Quark provides an overwhelming advantage in every scenario:

### ⚔️ Combat
Rule the battlefield with state-of-the-art combat modules.
> **KillAura, CrystalAura, AutoTotem, AnchorAura, BackstabAura, HitSelect, AutoArmor, TriggerBot, BowAimbot, TargetStrafe**

### 🏃‍♂️ Movement
Shatter the laws of physics.
> **ElytraFly, LongJump, Speed, Step, Velocity, Bhop, AirJump, Spider, SafeWalk, HighJump, FastFall**

### 🌍 World & Player
Automate your survival and dominate your environment.
> **Scaffold, AutoBuild, ChestStealer, Nuker, FastPlace, Freecam, AutoTool, InventoryManager, XCarry, Timer**

### 🐛 Exploit
Bend the server to your will using deep packet manipulation.
> **PacketFly, PingSpoof, Disabler, Phase, HighwayBuilder, ChatSpammer, AntiCrash, DeSync**

### 🎨 Render
See everything, everywhere.
> **ESP, Tracers, Fullbright, CustomFOV, Chams, FreeLook, BlockHighlight, Nametags, XRay, Hitbox**

---

## 🚀 Installation & Usage

### Prerequisites
Before building, ensure you have the following installed:
1. **Java 21 JDK**: Required for compiling Minecraft 1.21.1 mods.
2. **Git**: To clone the repository.
3. **Fabric Loader (0.16.2+)**: For your Minecraft client.

### 🛠️ Building from Source

1. Clone the repository to your local machine:
   ```bash
   git clone https://github.com/YourUsername/Minecraft-Hack-Client.git
   cd Minecraft-Hack-Client
   ```

2. Compile the client using Gradle (Windows):
   ```bash
   gradlew.bat build
   ```
   *(Or `./gradlew build` on macOS/Linux)*

3. Once compilation finishes successfully, your compiled `.jar` will be located in the `build/libs/` directory.
4. Drop the `quark-1.0.0.jar` into your `%appdata%\.minecraft\mods` folder.

### 💻 Running in Development

To launch the client directly from your IDE or terminal for debugging:
```bash
gradlew.bat runClient
```

---

## 🎮 Default Controls

- **Open ClickGUI:** Press `Right Shift` to open the interactive menu.
- **Bind Modules:** Right-click any module in the ClickGUI to assign a custom keyboard macro.
- **Chat Commands:** Prefix your commands with `.` in the in-game chat.
  - `.help` - List all commands
  - `.bind [module] [key]` - Bind a module
  - `.friend add [name]` - Add a player to your friends list
  - `.config save/load` - Manage your configurations

---

## 🛠️ Project Architecture

For developers looking to understand the codebase, Quark is structured cleanly using standard Fabric Mixins:

- `cc.quark.event`: The hyper-optimized `MethodHandle` EventBus.
- `cc.quark.mixin`: Low-level bytecode injection into Minecraft's engine (e.g., `MixinMinecraft`, `MixinClientConnection`, `MixinClientPlayerEntity`).
- `cc.quark.module`: The module registry containing all hack logic categorized by feature.
- `cc.quark.gui`: The custom ClickGUI and rendering utilities.
- `cc.quark.ghost`: Dedicated managers for silent aim, rotation limits, and attack delays.

---

## 🤝 Contributing

We welcome contributions from the community! If you have an idea for a new module, an anti-cheat bypass, or a bug fix:

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/EpicNewModule`).
3. Commit your changes (`git commit -m 'Add EpicNewModule'`).
4. Push to the branch (`git push origin feature/EpicNewModule`).
5. Open a Pull Request.

---

## ⚠️ Disclaimer & Warning

> **This project is strictly intended for educational and research purposes only.**
>
> The developers and contributors of Quark.cc are **NOT** responsible for any server bans, account suspensions, terms of service violations, or damages caused by the use or distribution of this software. By downloading, compiling, or using this source code, you agree to take full responsibility for your actions. Please respect server rules and use responsibly in singleplayer or private environments.

---

<div align="center">
  <p><i>Crafted with ❤️ and ☕ by the Quark.cc Development Team.</i></p>
</div>
