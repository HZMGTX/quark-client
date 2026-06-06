package cc.quark.agent;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Bootstrap - runs inside the target Minecraft JVM after injection.
 *
 * Uses ClassResolver (multi-environment aware) to locate MinecraftClient
 * regardless of Fabric/Forge/NeoForge/Vanilla/Lunar/Badlion.
 */
public class Bootstrap {

    private static volatile boolean initialized = false;

    public static void init(Instrumentation inst, ClassLoader mcClassLoader) {
        if (initialized) {
            System.out.println("[Quark] Already initialized, skipping.");
            return;
        }
        initialized = true;

        // Hand Instrumentation to ClassResolver so its structural scanner can work
        ClassResolver.setInstrumentation(inst);

        System.out.println("[Quark Bootstrap] Starting ("
            + EnvironmentDetector.getLoader() + " / "
            + EnvironmentDetector.getMinecraftVersion() + " / "
            + EnvironmentDetector.getLauncher() + ")");

        try {
            if (inst != null) {
                // Install our ASM class transformer
                inst.addTransformer(new ClassTransformer(), inst.isRetransformClassesSupported());
                System.out.println("[Quark Bootstrap] ClassTransformer installed.");

                // Retransform classes that are already loaded
                retransformKnownClasses(inst, mcClassLoader);
            }

            new Thread(() -> {
                try {
                    Thread.currentThread().setName("Quark-Bootstrap");
                    Object mcInstance = waitForMinecraft(mcClassLoader);
                    if (mcInstance == null) {
                        System.err.println("[Quark Bootstrap] MinecraftClient not found after 60 s!");
                        return;
                    }
                    System.out.println("[Quark Bootstrap] MC found: " + mcInstance.getClass().getName());

                    MinecraftHook.setup(mcInstance, mcClassLoader);

                    // Signal to user that injection worked
                    Thread.sleep(1500);
                    pingQuark(mcClassLoader);
                    System.out.println("[Quark Bootstrap] Injection complete! Press Right-Shift to open GUI.");

                } catch (Exception e) {
                    System.err.println("[Quark Bootstrap] Init thread error: " + e);
                    e.printStackTrace();
                }
            }, "Quark-Bootstrap").start();

        } catch (Exception e) {
            System.err.println("[Quark Bootstrap] Fatal: " + e);
            e.printStackTrace();
        }
    }

    // ── Retransform ───────────────────────────────────────────────────────────

    private static void retransformKnownClasses(Instrumentation inst, ClassLoader cl) {
        // Use ClassResolver to find the actual loaded classes (handles all loaders)
        Class<?>[] candidates = {
            ClassResolver.getMinecraftClient(),
            ClassResolver.getInGameHud(),
            ClassResolver.getGameRenderer(),
            ClassResolver.getNetHandler(),
            ClassResolver.getKeyboard(),
        };

        List<Class<?>> toRetransform = new ArrayList<>();
        for (Class<?> c : candidates) {
            if (c != null && inst.isModifiableClass(c)) {
                toRetransform.add(c);
                System.out.println("[Quark Bootstrap] Queued retransform: " + c.getName());
            }
        }

        if (!toRetransform.isEmpty()) {
            try {
                inst.retransformClasses(toRetransform.toArray(new Class[0]));
                System.out.println("[Quark Bootstrap] Retransformed " + toRetransform.size() + " class(es).");
            } catch (Exception e) {
                System.err.println("[Quark Bootstrap] Retransform failed: " + e.getMessage());
            }
        } else {
            System.out.println("[Quark Bootstrap] No modifiable classes found yet (will be patched on load).");
        }
    }

    // ── Wait for MC instance ──────────────────────────────────────────────────

    private static Object waitForMinecraft(ClassLoader cl) throws Exception {
        for (int i = 0; i < 120; i++) {
            // ClassResolver handles all loader/version variants
            Object inst = ClassResolver.getMinecraftInstance();
            if (inst != null) return inst;
            Thread.sleep(500);
        }
        return null;
    }

    // ── Signal injection success ──────────────────────────────────────────────

    private static void pingQuark(ClassLoader cl) {
        try {
            Class<?> quarkClass = Class.forName("cc.quark.Quark", false, cl);
            Method get = quarkClass.getDeclaredMethod("getInstance");
            get.setAccessible(true);
            Object q = get.invoke(null);
            if (q != null) {
                System.out.println("[Quark Bootstrap] Quark Fabric mod is loaded alongside agent.");
            }
        } catch (Exception ignored) {
            System.out.println("[Quark Bootstrap] Pure-inject mode (no Fabric mod present).");
        }
    }
}
