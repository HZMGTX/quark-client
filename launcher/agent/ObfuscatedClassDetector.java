package cc.quark.agent;

import org.objectweb.asm.*;

/**
 * ASM ClassVisitor that identifies which Minecraft class role an obfuscated class
 * fills, by inspecting method signatures and field patterns.
 *
 * Heuristics (in order of specificity):
 *   MinecraftClient  – has void noArg methods named "tick"/"run", plus Thread field, plus static self field
 *   InGameHud        – has void render(?,float) and a reference to MatrixStack or DrawContext
 *   GameRenderer     – has renderWorld or render(float,?) + Camera/Frustum fields
 *   NetHandler       – has sendPacket(?) and a reference to Connection/NetworkManager
 *   Keyboard         – has onKey(long,int,int,int) / key(long,int,int,int)
 */
public class ObfuscatedClassDetector extends ClassVisitor {

    // Counters from scanning
    private boolean hasVoidNoArgMethod   = false;
    private boolean hasSelfStaticField   = false;
    private boolean hasThreadField       = false;
    private boolean hasRenderMethod      = false;
    private boolean hasDrawContextField  = false;
    private boolean hasRenderWorldMethod = false;
    private boolean hasCameraField       = false;
    private boolean hasSendPacketMethod  = false;
    private boolean hasConnectionField   = false;
    private boolean hasOnKeyMethod       = false;

    private String className;

    public ObfuscatedClassDetector() {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version, int access, String name, String signature,
                      String superName, String[] interfaces) {
        this.className = name;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor,
                                   String signature, Object value) {
        // Self-referencing static field (singleton pattern)
        if ((access & Opcodes.ACC_STATIC) != 0 && className != null
                && descriptor.equals("L" + className + ";")) {
            hasSelfStaticField = true;
        }
        String lc = descriptor.toLowerCase();
        if (lc.contains("thread")) hasThreadField = true;
        if (lc.contains("drawcontext") || lc.contains("matrixstack") || lc.contains("guigraphics"))
            hasDrawContextField = true;
        if (lc.contains("camera") || lc.contains("frustum")) hasCameraField = true;
        if (lc.contains("connection") || lc.contains("networkmanager")
                || lc.contains("clientconnection") || lc.contains("networkhandler"))
            hasConnectionField = true;
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor,
                                     String signature, String[] exceptions) {
        // tick / run with no args → MinecraftClient candidate
        if ((access & Opcodes.ACC_STATIC) == 0
                && descriptor.equals("()V")
                && (name.equals("tick") || name.equals("run") || name.equals("func_71407_l")
                    || name.equals("method_1507") || name.equals("m_91383_"))) {
            hasVoidNoArgMethod = true;
        }

        // render(DrawContext/MatrixStack/GuiGraphics, float) → HUD candidate
        if ((access & Opcodes.ACC_STATIC) == 0
                && (name.equals("render") || name.equals("func_73830_a") || name.equals("method_1722"))
                && descriptor.endsWith(")V") && descriptor.contains("F")) {
            hasRenderMethod = true;
        }

        // renderWorld / renderLevel → GameRenderer candidate
        if ((access & Opcodes.ACC_STATIC) == 0
                && (name.equals("renderWorld") || name.equals("renderLevel")
                    || name.equals("func_195458_a") || name.equals("method_3153"))) {
            hasRenderWorldMethod = true;
        }

        // sendPacket(Packet) → NetHandler candidate
        if ((access & Opcodes.ACC_STATIC) == 0
                && (name.equals("sendPacket") || name.equals("func_147297_a")
                    || name.equals("method_2883") || name.equals("m_5475_"))
                && descriptor.endsWith(")V")) {
            hasSendPacketMethod = true;
        }

        // onKey / key(long, int, int, int, int, int) → Keyboard candidate
        if ((access & Opcodes.ACC_STATIC) == 0
                && (name.equals("onKey") || name.equals("key") || name.equals("method_1601"))
                && (descriptor.startsWith("(JIIII") || descriptor.startsWith("(JIII"))) {
            hasOnKeyMethod = true;
        }
        return null;
    }

    public ClassTransformer.ObfType getType() {
        if (hasOnKeyMethod) return ClassTransformer.ObfType.KEYBOARD;
        if (hasSendPacketMethod && hasConnectionField) return ClassTransformer.ObfType.NET_HANDLER;
        if (hasRenderWorldMethod && hasCameraField) return ClassTransformer.ObfType.GAME_RENDERER;
        if (hasRenderMethod && hasDrawContextField) return ClassTransformer.ObfType.IN_GAME_HUD;
        if (hasVoidNoArgMethod && hasSelfStaticField && hasThreadField)
            return ClassTransformer.ObfType.MINECRAFT_CLIENT;
        return ClassTransformer.ObfType.UNKNOWN;
    }
}
