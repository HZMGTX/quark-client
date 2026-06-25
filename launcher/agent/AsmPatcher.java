package cc.quark.agent;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Uses ASM to inject Quark event calls into Minecraft methods at runtime.
 * Each patched method calls MinecraftHook.onXxx() at its entry point.
 *
 * Supports multiple Minecraft versions / loaders:
 *   1.7-1.12  (old Forge MCP names)
 *   1.13-1.16 (Forge SRG / Fabric intermediary)
 *   1.17+     (Mojmap + Fabric Yarn / intermediary)
 *   Obfuscated variants (Lunar, Badlion, vanilla)
 */
public class AsmPatcher {

    private static final String HOOK = "cc/quark/agent/MinecraftHook";

    public static byte[] patch(byte[] bytes, String className, String patchTarget) {
        ClassReader  cr = new ClassReader(bytes);
        ClassWriter  cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        // Ensures only one render method per class receives the RENDER2D hook,
        // so the overlay is drawn exactly once per frame.
        final boolean[] render2dPatched = { false };

        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return switch (patchTarget) {
                    case "TICK"     -> maybeTickPatch(name, descriptor, access, mv);
                    case "RENDER2D" -> maybeRender2DPatch(name, descriptor, access, mv, render2dPatched);
                    case "RENDER3D" -> maybeRender3DPatch(name, descriptor, access, mv);
                    case "PACKET"   -> maybePacketPatch(name, descriptor, access, mv);
                    case "KEY"      -> maybeKeyPatch(name, descriptor, access, mv);
                    default         -> mv;
                };
            }
        }, 0);

        return cw.toByteArray();
    }

    // ── Tick: MinecraftClient.tick() ─────────────────────────────────────────
    // Names across versions:
    //   tick()           Fabric Yarn / Mojmap
    //   func_71407_l()   Old Forge MCP (< 1.17)
    //   method_1507()    Fabric intermediary
    //   m_91383_()       Forge Mojmap intermediary (1.17+)
    //   1-2 char names   obfuscated vanilla / Lunar

    private static MethodVisitor maybeTickPatch(String name, String desc, int access, MethodVisitor mv) {
        if (!"()V".equals(desc)) return mv;
        if ((access & Opcodes.ACC_STATIC) != 0) return mv;
        boolean match = name.equals("tick")
                     || name.equals("func_71407_l")
                     || name.equals("method_1507")
                     || name.equals("m_91383_")
                     || name.matches("[a-z]{1,3}");   // obfuscated short name
        if (!match) return mv;

        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ALOAD, 0);
                visitMethodInsn(INVOKESTATIC, HOOK, "onTick", "(Ljava/lang/Object;)V", false);
            }
        };
    }

    // ── Render2D: InGameHud render method ─────────────────────────────────────
    // The reliable, version-agnostic signal is the first parameter type, which is
    // always the 2D draw surface, regardless of mapping or MC version:
    //   1.20.2+  DrawContext   →  Lnet/minecraft/class_332;        (Fabric)
    //                              Lnet/minecraft/client/gui/DrawContext;
    //   Forge    GuiGraphics   →  Lnet/minecraft/client/gui/GuiGraphics;
    //   ≤1.20.1  MatrixStack   →  Lnet/minecraft/class_4587;
    // The second arg may be a float (≤1.20.1) or a RenderTickCounter object
    // (1.21+), so we don't depend on it — we pass 0f as the delta.
    //
    // We hook the first matching method in the class (guarded) so the overlay is
    // drawn exactly once per frame.

    private static final java.util.Set<String> DRAW_SURFACE_TYPES = java.util.Set.of(
        "net/minecraft/class_332",                  // DrawContext (intermediary)
        "net/minecraft/client/gui/DrawContext",     // DrawContext (Yarn)
        "net/minecraft/client/gui/GuiGraphics",     // GuiGraphics (Mojmap/Forge)
        "net/minecraft/class_4587",                 // MatrixStack (intermediary, ≤1.20.1)
        "com/mojang/blaze3d/vertex/PoseStack"       // PoseStack (Mojmap, ≤1.20.1)
    );

    private static MethodVisitor maybeRender2DPatch(String name, String desc, int access,
                                                    MethodVisitor mv, boolean[] alreadyPatched) {
        if (alreadyPatched[0]) return mv;
        if ((access & Opcodes.ACC_STATIC) != 0) return mv;
        if (!desc.endsWith(")V")) return mv;

        String first = firstParamType(desc);
        if (first == null || !DRAW_SURFACE_TYPES.contains(first)) return mv;

        alreadyPatched[0] = true;
        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ALOAD, 1);   // the draw surface (DrawContext / GuiGraphics)
                visitInsn(FCONST_0);      // delta = 0f (second arg type varies by version)
                visitMethodInsn(INVOKESTATIC, HOOK, "onRender2D", "(Ljava/lang/Object;F)V", false);
            }
        };
    }

    /** Returns the internal name of the first object parameter, or null. */
    private static String firstParamType(String desc) {
        int open = desc.indexOf('(') + 1;
        if (open <= 0 || open >= desc.length()) return null;
        char c = desc.charAt(open);
        if (c != 'L') return null;            // first param must be an object
        int semi = desc.indexOf(';', open);
        if (semi < 0) return null;
        return desc.substring(open + 1, semi);
    }

    // ── Render3D: GameRenderer.renderWorld / renderLevel ────────────────────
    // 1.21+  renderWorld(RenderTickCounter)
    // 1.17+  renderWorld(float, long, MatrixStack)
    // <1.17  renderWorld(float, long)

    private static MethodVisitor maybeRender3DPatch(String name, String desc, int access, MethodVisitor mv) {
        if ((access & Opcodes.ACC_STATIC) != 0) return mv;
        boolean nameOk = name.equals("renderWorld")
                      || name.equals("renderLevel")
                      || name.equals("func_195458_a")
                      || name.equals("method_3153");
        if (!nameOk) return mv;

        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ALOAD, 0);   // this (GameRenderer)
                visitMethodInsn(INVOKESTATIC, HOOK, "onRender3D", "(Ljava/lang/Object;)V", false);
            }
        };
    }

    // ── Packet send: ClientPlayNetworkHandler.sendPacket(Packet) ────────────

    private static MethodVisitor maybePacketPatch(String name, String desc, int access, MethodVisitor mv) {
        if ((access & Opcodes.ACC_STATIC) != 0) return mv;
        boolean nameOk = name.equals("sendPacket")
                      || name.equals("send")
                      || name.equals("func_147297_a")
                      || name.equals("method_2883")
                      || name.equals("m_5475_");
        if (!nameOk) return mv;
        if (!desc.endsWith(")V") || desc.equals("()V")) return mv;

        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ALOAD, 1);   // Packet<?>
                visitMethodInsn(INVOKESTATIC, HOOK, "onPacketSend", "(Ljava/lang/Object;)V", false);
            }
        };
    }

    // ── Key: Keyboard.onKey(long window, int key, int scancode, int action, int mods) ─
    // arg slots: 0=this 1=window(long,2slots) 3=key 4=scancode 5=action 6=mods

    private static MethodVisitor maybeKeyPatch(String name, String desc, int access, MethodVisitor mv) {
        if ((access & Opcodes.ACC_STATIC) != 0) return mv;
        boolean nameOk = name.equals("onKey")
                      || name.equals("key")
                      || name.equals("method_1601");
        if (!nameOk) return mv;
        if (!desc.startsWith("(J")) return mv; // first arg must be long (window handle)

        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ILOAD, 3);   // key (int, after long window which takes 2 slots)
                visitVarInsn(ILOAD, 5);   // action (int)
                visitMethodInsn(INVOKESTATIC, HOOK, "onKey", "(II)V", false);
            }
        };
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** True if descriptor has exactly 2 parameters. */
    private static boolean hasTwoParams(String desc) {
        // Simple check: parse param section
        int open = desc.indexOf('(') + 1;
        int close = desc.indexOf(')');
        if (open < 0 || close < 0) return false;
        String params = desc.substring(open, close);
        return countParams(params) == 2;
    }

    private static int countParams(String params) {
        int count = 0, i = 0;
        while (i < params.length()) {
            char c = params.charAt(i);
            if (c == 'L') { count++; i = params.indexOf(';', i) + 1; }
            else if (c == '[') { i++; }
            else { count++; i++; }
        }
        return count;
    }
}
