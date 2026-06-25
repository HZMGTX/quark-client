package cc.quark.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Resolves Minecraft class and method names at runtime, regardless of:
 *  - Mod loader (Fabric/Forge/NeoForge/Vanilla)
 *  - Minecraft version (1.7 – 1.21+)
 *  - Launcher (Official/TLauncher/Prism/MultiMC/Lunar/etc.)
 *  - Obfuscation level (Yarn / Intermediary / Mojang / SRG / Proguard)
 *
 * Strategy:
 *  1. Try deobfuscated names for each known loader
 *  2. Try intermediary names (Fabric production)
 *  3. Scan all loaded classes by structural signature (singleton, player field, etc.)
 */
public class ClassResolver {

    private static final Map<String, Class<?>> CACHE = new HashMap<>();
    private static Instrumentation instrumentation;

    public static void setInstrumentation(Instrumentation inst) {
        instrumentation = inst;
    }

    // ── Known name tables ─────────────────────────────────────────────────────

    // MinecraftClient / Minecraft
    private static final String[] MC_NAMES = {
        // Fabric Yarn (deobfuscated dev / Quark's own build)
        "net.minecraft.client.MinecraftClient",
        // Forge / NeoForge (Mojang mappings)
        "net.minecraft.client.Minecraft",
        // Fabric intermediary (production runtime)
        "net.minecraft.class_310",
        // Very old Forge (MCP names, pre-1.17)
        "net.minecraft.client.Minecraft",
        // Lunar / Badlion repackaged name (common)
        "bpw",  "bzd",  "byq",  "cb",  "cg",  "cd",
    };

    private static final String[] PLAYER_NAMES = {
        "net.minecraft.client.network.ClientPlayerEntity",   // Fabric Yarn
        "net.minecraft.client.player.LocalPlayer",           // Forge Mojmap
        "net.minecraft.class_746",                           // Fabric intermediary
        "net.minecraft.client.entity.EntityPlayerSP",        // Old Forge
        "bdp", "bdi", "bcz",                                 // obfuscated variants
    };

    private static final String[] WORLD_NAMES = {
        "net.minecraft.client.world.ClientWorld",
        "net.minecraft.world.level.Level",
        "net.minecraft.class_638",
        "net.minecraft.client.multiplayer.ClientLevel",
        "net.minecraft.world.World",
    };

    private static final String[] INGAME_HUD_NAMES = {
        "net.minecraft.client.gui.hud.InGameHud",   // Fabric Yarn
        "net.minecraft.client.gui.Gui",             // Forge Mojmap
        "net.minecraft.class_329",                  // Fabric intermediary (1.21.x)
    };

    private static final String[] GAME_RENDERER_NAMES = {
        "net.minecraft.client.render.GameRenderer",
        "net.minecraft.client.renderer.GameRenderer",
        "net.minecraft.class_757",
    };

    private static final String[] NET_HANDLER_NAMES = {
        "net.minecraft.client.network.ClientPlayNetworkHandler",
        "net.minecraft.client.multiplayer.ClientPacketListener",
        "net.minecraft.class_634",
        "net.minecraft.client.network.NetHandlerPlayClient",
    };

    private static final String[] KEYBOARD_NAMES = {
        "net.minecraft.client.Keyboard",
        "net.minecraft.class_309",
        "net.minecraft.client.KeyboardHandler",
    };

    // ── Public lookup API ─────────────────────────────────────────────────────

    public static Class<?> getMinecraftClient()    { return resolve("mc",      MC_NAMES,          ClassResolver::scanForMinecraftClient); }
    public static Class<?> getPlayerClass()        { return resolve("player",  PLAYER_NAMES,      null); }
    public static Class<?> getWorldClass()         { return resolve("world",   WORLD_NAMES,       null); }
    public static Class<?> getInGameHud()          { return resolve("hud",     INGAME_HUD_NAMES,  ClassResolver::scanForHud); }
    public static Class<?> getGameRenderer()       { return resolve("gr",      GAME_RENDERER_NAMES, null); }
    public static Class<?> getNetHandler()         { return resolve("net",     NET_HANDLER_NAMES, null); }
    public static Class<?> getKeyboard()           { return resolve("kb",      KEYBOARD_NAMES,    null); }

    // ── Method resolution ─────────────────────────────────────────────────────

    /** Finds the game tick method on MinecraftClient (no args, void return). */
    public static Method getTickMethod(Class<?> mc) {
        // Try known names first
        for (String name : new String[]{"tick", "func_71407_l", "method_1507", "m_91383_"}) {
            try {
                Method m = mc.getDeclaredMethod(name);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {}
        }
        // Scan: void method with no params, non-static, named tick-like
        return scanMethod(mc, "tick", "()V");
    }

    /** Finds the render method on InGameHud: render(DrawContext/MatrixStack, float) */
    public static Method getRenderMethod(Class<?> hud) {
        for (String name : new String[]{"render", "func_73830_a", "method_1722", "m_168686_"}) {
            for (Method m : hud.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == 2) {
                    m.setAccessible(true); return m;
                }
            }
        }
        return null;
    }

    /** Finds sendPacket on ClientPlayNetworkHandler */
    public static Method getSendPacketMethod(Class<?> handler) {
        for (String name : new String[]{"sendPacket", "func_147297_a", "method_2883", "m_5475_"}) {
            for (Method m : handler.getDeclaredMethods()) {
                if (m.getName().equals(name) && m.getParameterCount() == 1) {
                    m.setAccessible(true); return m;
                }
            }
        }
        return null;
    }

    // ── Singleton instance getter ─────────────────────────────────────────────

    /** Gets the running MinecraftClient instance. */
    public static Object getMinecraftInstance() {
        Class<?> mc = getMinecraftClient();
        if (mc == null) return null;

        // Try static getInstance()
        for (String n : new String[]{"getInstance", "getMinecraft", "func_71410_x", "method_1551"}) {
            try {
                Method m = mc.getDeclaredMethod(n);
                m.setAccessible(true);
                Object inst = m.invoke(null);
                if (inst != null) return inst;
            } catch (Exception ignored) {}
        }
        // Try static field 'instance', 'theMinecraft', 'INSTANCE'
        for (Field f : mc.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == mc) {
                try {
                    f.setAccessible(true);
                    Object inst = f.get(null);
                    if (inst != null) return inst;
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private interface Scanner { Class<?> scan(); }

    private static Class<?> resolve(String key, String[] names, Scanner fallback) {
        if (CACHE.containsKey(key)) return CACHE.get(key);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (String name : names) {
            try {
                Class<?> c = Class.forName(name, false, cl);
                CACHE.put(key, c);
                System.out.println("[ClassResolver] Resolved " + key + " = " + c.getName());
                return c;
            } catch (ClassNotFoundException ignored) {}
        }

        // Try all loaded classes via instrumentation
        if (instrumentation != null) {
            for (Class<?> c : instrumentation.getAllLoadedClasses()) {
                for (String name : names) {
                    if (c.getName().equals(name)) {
                        CACHE.put(key, c);
                        return c;
                    }
                }
            }
        }

        // Structural scan fallback
        if (fallback != null) {
            Class<?> c = fallback.scan();
            if (c != null) {
                CACHE.put(key, c);
                System.out.println("[ClassResolver] Scanned " + key + " = " + c.getName());
                return c;
            }
        }

        System.err.println("[ClassResolver] WARN: Could not resolve " + key);
        return null;
    }

    /**
     * Finds MinecraftClient by structural signature:
     *  - Has a static field of its own type (singleton)
     *  - Has a non-static field whose type name contains "player" (case-insensitive)
     *  - Has a non-static field whose type name contains "world" or "level"
     */
    private static Class<?> scanForMinecraftClient() {
        if (instrumentation == null) return null;
        for (Class<?> c : instrumentation.getAllLoadedClasses()) {
            if (!looksLikeMinecraftClient(c)) continue;
            return c;
        }
        return null;
    }

    private static boolean looksLikeMinecraftClient(Class<?> c) {
        try {
            boolean hasSelfField = false, hasPlayerField = false, hasWorldField = false;
            for (Field f : c.getDeclaredFields()) {
                String typeName = f.getType().getName().toLowerCase();
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == c) hasSelfField = true;
                if (!Modifier.isStatic(f.getModifiers()) && typeName.contains("player"))  hasPlayerField = true;
                if (!Modifier.isStatic(f.getModifiers()) && (typeName.contains("world") || typeName.contains("level"))) hasWorldField = true;
            }
            return hasSelfField && hasPlayerField && hasWorldField;
        } catch (Throwable t) { return false; }
    }

    private static Class<?> scanForHud() {
        if (instrumentation == null) return null;
        for (Class<?> c : instrumentation.getAllLoadedClasses()) {
            try {
                boolean hasRender = false;
                for (Method m : c.getDeclaredMethods()) {
                    if (m.getParameterCount() == 2 && m.getReturnType() == void.class
                        && (m.getName().equals("render") || m.getName().equals("func_73830_a"))) {
                        hasRender = true; break;
                    }
                }
                if (!hasRender) continue;
                // Must have a MinecraftClient field
                for (Field f : c.getDeclaredFields()) {
                    Class<?> mc = getMinecraftClient();
                    if (mc != null && f.getType() == mc) return c;
                }
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static Method scanMethod(Class<?> cls, String hint, String descriptor) {
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getParameterCount() == 0 && m.getReturnType() == void.class
                && !Modifier.isStatic(m.getModifiers())
                && m.getName().toLowerCase().contains(hint.toLowerCase())) {
                m.setAccessible(true); return m;
            }
        }
        return null;
    }
}
