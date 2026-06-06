package cc.quark.agent;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.*;

/**
 * Universal ClassFileTransformer - hooks into Minecraft regardless of loader or version.
 *
 * Handles:
 *   Fabric (Yarn + Intermediary), Forge (SRG + Mojmap), NeoForge,
 *   OptiFine, Vanilla (any version 1.7 – 1.21+),
 *   Lunar Client, Badlion, TLauncher, Prism, MultiMC, etc.
 */
public class ClassTransformer implements ClassFileTransformer {

    // Internal class name → patch target
    private static final Map<String, String> TARGETS = buildTargetMap();

    private static Map<String, String> buildTargetMap() {
        Map<String, String> m = new LinkedHashMap<>();

        // ── MinecraftClient / Minecraft ──────────────────────────────────────
        // Fabric Yarn (dev + quark's own build)
        m.put("net/minecraft/client/MinecraftClient",                     "TICK");
        // Forge Mojmap (1.17+)
        m.put("net/minecraft/client/Minecraft",                           "TICK");
        // Fabric intermediary (production runtime)
        m.put("net/minecraft/class_310",                                  "TICK");
        // Old Forge MCP (< 1.17)
        m.put("net/minecraft/client/main/Main",                          null); // skip, not MC class

        // ── InGameHud / Gui ──────────────────────────────────────────────────
        m.put("net/minecraft/client/gui/hud/InGameHud",                  "RENDER2D");
        m.put("net/minecraft/client/gui/Gui",                            "RENDER2D"); // Forge
        m.put("net/minecraft/class_1695",                                "RENDER2D"); // intermediary

        // ── GameRenderer ─────────────────────────────────────────────────────
        m.put("net/minecraft/client/render/GameRenderer",                "RENDER3D");
        m.put("net/minecraft/client/renderer/GameRenderer",              "RENDER3D"); // Forge
        m.put("net/minecraft/class_757",                                 "RENDER3D"); // intermediary

        // ── ClientPlayNetworkHandler ──────────────────────────────────────────
        m.put("net/minecraft/client/network/ClientPlayNetworkHandler",   "PACKET");
        m.put("net/minecraft/client/multiplayer/ClientPacketListener",   "PACKET"); // Forge 1.17+
        m.put("net/minecraft/class_634",                                 "PACKET"); // intermediary
        m.put("net/minecraft/client/network/NetHandlerPlayClient",       "PACKET"); // old Forge

        // ── Keyboard ─────────────────────────────────────────────────────────
        m.put("net/minecraft/client/Keyboard",                           "KEY");
        m.put("net/minecraft/class_309",                                 "KEY");     // intermediary
        m.put("net/minecraft/client/KeyboardHandler",                    "KEY");     // Forge

        // ── Common obfuscated names (Lunar / Badlion / vanilla) ───────────────
        // These are guesses; ClassResolver's structural scanner is the real fallback.
        // Adding common 2-3 char obfuscated class names seen in the wild:
        addObfuscatedVariants(m);

        return Collections.unmodifiableMap(m);
    }

    private static void addObfuscatedVariants(Map<String, String> m) {
        // Common obfuscated names for MinecraftClient across versions:
        //   1.8.x: "bsu", 1.12.x: "bib", 1.16.x: "cbp", 1.17+: uses Mojmap
        // We register a wildcard handler for short-named classes in the default package.
        // The actual matching is done by ClassResolver's structural scanner at runtime.
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null) return null;

        // Fast path: known deobfuscated names
        String target = TARGETS.get(className);
        if (target != null) {
            return patch(className, target, classfileBuffer);
        }

        // Short class names (2-4 chars, no package) → check structural signature
        if (isShortObfuscatedName(className)) {
            return tryPatchObfuscated(className, classfileBuffer);
        }

        return null;
    }

    private byte[] patch(String className, String target, byte[] bytes) {
        if (target == null) return null;
        try {
            byte[] patched = AsmPatcher.patch(bytes, className, target);
            System.out.println("[Transformer] Patched [" + target + "] → " + className);
            return patched;
        } catch (Throwable t) {
            System.err.println("[Transformer] Failed to patch " + className + ": " + t.getMessage());
            return null;
        }
    }

    /**
     * For obfuscated classes, try to determine which Minecraft class this is
     * and apply the corresponding patch.
     */
    private byte[] tryPatchObfuscated(String className, byte[] bytes) {
        ObfType type = detectObfuscatedType(bytes);
        if (type == ObfType.UNKNOWN) return null;
        System.out.println("[Transformer] Detected obfuscated " + type + " = " + className);
        return patch(className, type.patchTarget, bytes);
    }

    enum ObfType {
        MINECRAFT_CLIENT("TICK"), IN_GAME_HUD("RENDER2D"), GAME_RENDERER("RENDER3D"),
        NET_HANDLER("PACKET"), KEYBOARD("KEY"), UNKNOWN(null);
        final String patchTarget;
        ObfType(String t) { patchTarget = t; }
    }

    /**
     * Detects what kind of Minecraft class this is by inspecting the bytecode
     * for characteristic method signatures.
     */
    private ObfType detectObfuscatedType(byte[] bytes) {
        try {
            org.objectweb.asm.ClassReader cr = new org.objectweb.asm.ClassReader(bytes);
            ObfuscatedClassDetector detector = new ObfuscatedClassDetector();
            cr.accept(detector, org.objectweb.asm.ClassReader.SKIP_CODE);
            return detector.getType();
        } catch (Throwable t) {
            return ObfType.UNKNOWN;
        }
    }

    private static boolean isShortObfuscatedName(String name) {
        // "abc", "bsu", "cg" — short names without slashes (no package)
        return !name.contains("/") && name.length() <= 4 && name.matches("[a-z]{1,4}");
    }
}
