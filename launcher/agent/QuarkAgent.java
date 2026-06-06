package cc.quark.agent;

import java.lang.instrument.Instrumentation;

/**
 * Quark.cc Java Agent - entry point when loaded via JVM Attach API or
 * via the C++ DLL injector.
 *
 * Usage (Java Attach):
 *   java -jar quark-injector.jar <minecraft-pid>
 *
 * Usage (C++ DLL):
 *   The DLL loads this agent using JNI's JVM_attach mechanism.
 *
 * The agent works in two modes:
 *   1. Fabric mode  - Minecraft already has Fabric loaded; we wire into the
 *                     existing Quark EventBus (all 1300+ modules work).
 *   2. Standalone   - Vanilla Minecraft; we use ASM hooks + MinecraftHook
 *                     to provide tick/render/packet/key events independently.
 */
public class QuarkAgent {

    private static volatile boolean loaded = false;

    // ── Called by JVM Attach API (runtime injection) ──────────────────────────

    public static void agentmain(String agentArgs, Instrumentation inst) {
        if (loaded) return;
        loaded = true;

        printBanner();
        ClassLoader cl = findMinecraftClassLoader(inst);
        Bootstrap.init(inst, cl != null ? cl : Thread.currentThread().getContextClassLoader());
    }

    // ── Called if added to JVM startup args (-javaagent:) ────────────────────

    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

    // ── Called from C++ DLL via JNI ───────────────────────────────────────────

    public static void injectFromDll() {
        if (loaded) return;
        loaded = true;

        printBanner();
        // In DLL mode we don't have Instrumentation - use reflection-only hooks
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Bootstrap.init(null, cl);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Finds the ClassLoader that has loaded Minecraft classes.
     * With Fabric this is the FabricLaunchHandler's loader.
     */
    private static ClassLoader findMinecraftClassLoader(Instrumentation inst) {
        if (inst == null) return null;
        for (Class<?> cls : inst.getAllLoadedClasses()) {
            String n = cls.getName();
            if ((n.equals("net.minecraft.client.MinecraftClient")
              || n.equals("net.minecraft.client.Minecraft"))
              && cls.getClassLoader() != null) {
                return cls.getClassLoader();
            }
        }
        return null;
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ██████╗ ██╗   ██╗ █████╗ ██████╗ ██╗  ██╗ ██████╗ ██████╗ ");
        System.out.println(" ██╔═══██╗██║   ██║██╔══██╗██╔══██╗██║ ██╔╝██╔════╝██╔════╝ ");
        System.out.println(" ██║   ██║██║   ██║███████║██████╔╝█████╔╝ ██║     ██║      ");
        System.out.println(" ██║▄▄ ██║██║   ██║██╔══██║██╔══██╗██╔═██╗ ██║     ██║      ");
        System.out.println(" ╚██████╔╝╚██████╔╝██║  ██║██║  ██║██║  ██╗╚██████╗╚██████╗ ");
        System.out.println("  ╚══▀▀═╝  ╚═════╝ ╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═════╝");
        System.out.println("                    Ghost Client  v1.0  |  Injection Mode");
        System.out.println("  ─────────────────────────────────────────────────────────── ");
        System.out.println();
    }
}
