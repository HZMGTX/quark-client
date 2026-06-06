package cc.quark.agent;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

/**
 * ASM-based class file transformer that instruments key Minecraft classes
 * to fire Quark events. Works when injected at runtime (no Fabric Mixin needed).
 *
 * Target methods (Fabric/Yarn deobfuscated names):
 *   MinecraftClient.tick()                             → EventTick
 *   InGameHud.render(DrawContext, float)               → EventRender2D
 *   GameRenderer.renderWorld(RenderTickCounter)        → EventRender3D
 *   ClientPlayNetworkHandler.sendPacket(Packet)        → EventPacketSend
 *   Keyboard.onKey(long, int, int, int, int, int)      → EventKey
 */
public class ClassTransformer implements ClassFileTransformer {

    // Map of internal class names → transformer method name
    private static final Map<String, String> TARGETS = new HashMap<>();

    static {
        // Fabric (Yarn) deobfuscated names
        TARGETS.put("net/minecraft/client/MinecraftClient",                          "transformTick");
        TARGETS.put("net/minecraft/client/gui/hud/InGameHud",                       "transformRender2D");
        TARGETS.put("net/minecraft/client/render/GameRenderer",                     "transformRender3D");
        TARGETS.put("net/minecraft/client/network/ClientPlayNetworkHandler",        "transformPacketSend");
        TARGETS.put("net/minecraft/client/Keyboard",                                "transformKey");

        // Mojang official mapping variants (for launchers that use official mappings)
        TARGETS.put("com/mojang/blaze3d/systems/RenderSystem",                      null); // no-op guard
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (className == null || !TARGETS.containsKey(className)) return null;
        String method = TARGETS.get(className);
        if (method == null) return null;

        try {
            return AsmPatcher.patch(classfileBuffer, className, method);
        } catch (Throwable t) {
            System.err.println("[Quark Transformer] Failed to patch " + className + ": " + t.getMessage());
            return null; // Return null = use original bytes
        }
    }
}
