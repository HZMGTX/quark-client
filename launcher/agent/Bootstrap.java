package cc.quark.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * Bootstrap - runs inside the target Minecraft JVM after injection.
 * Finds MinecraftClient via reflection, loads Quark modules, and
 * installs the ClassTransformer for event hooks.
 */
public class Bootstrap {

    private static volatile boolean initialized = false;

    public static void init(Instrumentation inst, ClassLoader mcClassLoader) {
        if (initialized) {
            System.out.println("[Quark] Already initialized, skipping.");
            return;
        }
        initialized = true;

        System.out.println("[Quark Bootstrap] Starting initialization...");

        try {
            // 1. Install ASM class transformer for Minecraft hooks
            if (inst != null) {
                inst.addTransformer(new ClassTransformer(), inst.isRetransformClassesSupported());
                System.out.println("[Quark Bootstrap] ClassTransformer installed.");

                // Retransform already-loaded Minecraft classes
                retransformMinecraftClasses(inst, mcClassLoader);
            }

            // 2. Wait for MinecraftClient to be ready, then init our system
            new Thread(() -> {
                try {
                    Thread.currentThread().setName("Quark-Bootstrap");
                    Object mcInstance = waitForMinecraft(mcClassLoader);
                    if (mcInstance == null) {
                        System.err.println("[Quark Bootstrap] Failed to find MinecraftClient!");
                        return;
                    }
                    System.out.println("[Quark Bootstrap] Found MinecraftClient: " + mcInstance.getClass().getName());

                    // 3. Initialize the hook dispatcher with the MC instance
                    MinecraftHook.setup(mcInstance, mcClassLoader);

                    // 4. Simulate Right-Shift to open GUI (signals injection success)
                    Thread.sleep(1500);
                    triggerGui(mcClassLoader);

                    System.out.println("[Quark Bootstrap] Injection complete! Press Right-Shift to open GUI.");

                } catch (Exception e) {
                    System.err.println("[Quark Bootstrap] Init error: " + e.getMessage());
                    e.printStackTrace();
                }
            }, "Quark-Bootstrap").start();

        } catch (Exception e) {
            System.err.println("[Quark Bootstrap] Fatal init error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void retransformMinecraftClasses(Instrumentation inst, ClassLoader cl) {
        String[] targets = {
            "net.minecraft.client.MinecraftClient",
            "net.minecraft.client.gui.hud.InGameHud",
            "net.minecraft.client.render.GameRenderer",
            "net.minecraft.client.network.ClientPlayNetworkHandler",
            "net.minecraft.client.Keyboard"
        };

        List<Class<?>> toRetransform = new ArrayList<>();
        for (String name : targets) {
            try {
                Class<?> cls = Class.forName(name, false, cl);
                if (inst.isModifiableClass(cls)) {
                    toRetransform.add(cls);
                    System.out.println("[Quark Bootstrap] Queued for retransform: " + name);
                }
            } catch (ClassNotFoundException e) {
                // Try intermediary name variants
                try {
                    Class<?> cls = findClassBySimpleName(inst, name.substring(name.lastIndexOf('.') + 1));
                    if (cls != null && inst.isModifiableClass(cls)) {
                        toRetransform.add(cls);
                    }
                } catch (Exception ignored) {}
            }
        }

        if (!toRetransform.isEmpty()) {
            try {
                inst.retransformClasses(toRetransform.toArray(new Class[0]));
                System.out.println("[Quark Bootstrap] Retransformed " + toRetransform.size() + " classes.");
            } catch (Exception e) {
                System.err.println("[Quark Bootstrap] Retransform failed: " + e.getMessage());
            }
        }
    }

    private static Class<?> findClassBySimpleName(Instrumentation inst, String simpleName) {
        for (Class<?> c : inst.getAllLoadedClasses()) {
            if (c.getSimpleName().equals(simpleName)) return c;
        }
        return null;
    }

    /**
     * Spins until MinecraftClient.getInstance() returns a non-null client with a player world.
     */
    private static Object waitForMinecraft(ClassLoader cl) throws Exception {
        String[] candidateClasses = {
            "net.minecraft.client.MinecraftClient",
            "net.minecraft.client.Minecraft"     // fallback for older/modded names
        };

        for (int attempt = 0; attempt < 120; attempt++) {
            for (String className : candidateClasses) {
                try {
                    Class<?> mc = Class.forName(className, false, cl);
                    // Try getInstance() static method
                    try {
                        Method getInstance = mc.getDeclaredMethod("getInstance");
                        getInstance.setAccessible(true);
                        Object instance = getInstance.invoke(null);
                        if (instance != null) return instance;
                    } catch (NoSuchMethodException ignored) {}

                    // Try static field 'instance' or 'INSTANCE'
                    for (String fieldName : new String[]{"instance", "INSTANCE", "_instance"}) {
                        try {
                            Field f = mc.getDeclaredField(fieldName);
                            f.setAccessible(true);
                            Object instance = f.get(null);
                            if (instance != null) return instance;
                        } catch (NoSuchFieldException ignored) {}
                    }
                } catch (ClassNotFoundException ignored) {}
            }
            Thread.sleep(500);
        }
        return null;
    }

    private static void triggerGui(ClassLoader cl) {
        // Signal the GUI open by posting to Quark's event system if it was bootstrapped
        try {
            Class<?> quark = Class.forName("cc.quark.Quark", false, cl);
            Method getInstance = quark.getDeclaredMethod("getInstance");
            getInstance.setAccessible(true);
            Object quarkInst = getInstance.invoke(null);
            if (quarkInst != null) {
                System.out.println("[Quark Bootstrap] Quark instance found, client is live!");
            }
        } catch (Exception e) {
            System.out.println("[Quark Bootstrap] Quark not loaded as Fabric mod - running in pure inject mode.");
        }
    }
}
