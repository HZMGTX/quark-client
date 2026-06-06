package cc.quark.agent;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * Uses ASM to inject Quark event calls into Minecraft methods at runtime.
 * Each patched method calls MinecraftHook.onXxx() at its entry point.
 */
public class AsmPatcher {

    private static final String HOOK_CLASS = "cc/quark/agent/MinecraftHook";

    public static byte[] patch(byte[] bytes, String className, String patchMethod) {
        ClassReader cr = new ClassReader(bytes);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        cr.accept(new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                return switch (patchMethod) {
                    case "transformTick"       -> patchTick(name, descriptor, access, mv, className);
                    case "transformRender2D"   -> patchRender2D(name, descriptor, access, mv, className);
                    case "transformRender3D"   -> patchRender3D(name, descriptor, access, mv, className);
                    case "transformPacketSend" -> patchPacketSend(name, descriptor, access, mv, className);
                    case "transformKey"        -> patchKey(name, descriptor, access, mv, className);
                    default                    -> mv;
                };
            }
        }, 0);

        return cw.toByteArray();
    }

    // ── MinecraftClient.tick() ────────────────────────────────────────────────

    private static MethodVisitor patchTick(String name, String desc, int access,
                                           MethodVisitor mv, String owner) {
        if (!"tick".equals(name) || !"()V".equals(desc)) return mv;
        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ALOAD, 0); // this (MinecraftClient)
                invokeStatic(Type.getType("L" + HOOK_CLASS + ";"),
                    new Method("onTick", "(Ljava/lang/Object;)V"));
            }
        };
    }

    // ── InGameHud.render(DrawContext, float) ──────────────────────────────────

    private static MethodVisitor patchRender2D(String name, String desc, int access,
                                               MethodVisitor mv, String owner) {
        if (!"render".equals(name)) return mv;
        if (!desc.contains("DrawContext") && !desc.contains("F")) return mv;
        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ALOAD, 1); // DrawContext
                visitVarInsn(FLOAD, 2); // float tickDelta
                invokeStatic(Type.getType("L" + HOOK_CLASS + ";"),
                    new Method("onRender2D", "(Ljava/lang/Object;F)V"));
            }
        };
    }

    // ── GameRenderer.renderWorld(RenderTickCounter) ───────────────────────────

    private static MethodVisitor patchRender3D(String name, String desc, int access,
                                               MethodVisitor mv, String owner) {
        if (!"renderWorld".equals(name)) return mv;
        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ALOAD, 0); // this (GameRenderer)
                invokeStatic(Type.getType("L" + HOOK_CLASS + ";"),
                    new Method("onRender3D", "(Ljava/lang/Object;)V"));
            }
        };
    }

    // ── ClientPlayNetworkHandler.sendPacket(Packet) ───────────────────────────

    private static MethodVisitor patchPacketSend(String name, String desc, int access,
                                                  MethodVisitor mv, String owner) {
        if (!"sendPacket".equals(name)) return mv;
        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ALOAD, 1); // Packet<?>
                invokeStatic(Type.getType("L" + HOOK_CLASS + ";"),
                    new Method("onPacketSend", "(Ljava/lang/Object;)V"));
            }
        };
    }

    // ── Keyboard.onKey(long, int, int, int, int, int) ────────────────────────

    private static MethodVisitor patchKey(String name, String desc, int access,
                                          MethodVisitor mv, String owner) {
        if (!"onKey".equals(name)) return mv;
        return new AdviceAdapter(Opcodes.ASM9, mv, access, name, desc) {
            @Override
            protected void onMethodEnter() {
                visitVarInsn(ILOAD, 3); // key code (int)
                visitVarInsn(ILOAD, 5); // action (int)
                invokeStatic(Type.getType("L" + HOOK_CLASS + ";"),
                    new Method("onKey", "(II)V"));
            }
        };
    }

    // Helper: AdviceAdapter.invokeStatic shortcut
    private static class Method {
        final String name; final String desc;
        Method(String name, String desc) { this.name = name; this.desc = desc; }
    }

    private static void invokeStatic(AdviceAdapter aa, Type owner, Method m) {
        aa.visitMethodInsn(Opcodes.INVOKESTATIC, owner.getInternalName(), m.name, m.desc, false);
    }
}
