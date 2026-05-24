package cc.quark.mixin;

import cc.quark.Quark;
import cc.quark.command.CommandManager;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventTick;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraft {

    /**
     * Post EventTick every game tick so modules can react without subscribing
     * to Fabric's tick event directly.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Quark instance = Quark.getInstance();
        if (instance == null) return;
        instance.getEventBus().post(new EventTick());
    }

    /**
     * Fire EventKey for every keyboard key that is currently pressed so that
     * module keybinds are handled through the event bus without polling.
     *
     * We iterate the common alphabetic and function-key range on each input
     * event flush.  Keys are posted only while they transition from released
     * to pressed (GLFW_PRESS) to avoid repeat-spam.
     */
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInput(CallbackInfo ci) {
        Quark instance = Quark.getInstance();
        if (instance == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        long window = mc.getWindow().getHandle();

        // Check A-Z (65-90), F1-F12 (290-301), and a handful of extras
        int[] keyCodes = buildKeyCodeRange();
        for (int key : keyCodes) {
            if (GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS) {
                instance.getEventBus().post(new EventKey(key));
            }
        }
    }

    /** Build the array of GLFW key codes we monitor for module keybinds. */
    private static int[] buildKeyCodeRange() {
        // A-Z, 0-9, F1-F12, common specials
        int[] extras = {
            GLFW.GLFW_KEY_F1, GLFW.GLFW_KEY_F2, GLFW.GLFW_KEY_F3, GLFW.GLFW_KEY_F4,
            GLFW.GLFW_KEY_F5, GLFW.GLFW_KEY_F6, GLFW.GLFW_KEY_F7, GLFW.GLFW_KEY_F8,
            GLFW.GLFW_KEY_F9, GLFW.GLFW_KEY_F10, GLFW.GLFW_KEY_F11, GLFW.GLFW_KEY_F12,
            GLFW.GLFW_KEY_HOME, GLFW.GLFW_KEY_END, GLFW.GLFW_KEY_INSERT,
            GLFW.GLFW_KEY_DELETE, GLFW.GLFW_KEY_RIGHT_SHIFT, GLFW.GLFW_KEY_LEFT_SHIFT,
            GLFW.GLFW_KEY_RIGHT_CONTROL, GLFW.GLFW_KEY_LEFT_CONTROL,
            GLFW.GLFW_KEY_RIGHT_ALT, GLFW.GLFW_KEY_LEFT_ALT
        };
        // A(65)..Z(90) = 26, 0(48)..9(57) = 10
        int total = 26 + 10 + extras.length;
        int[] keys = new int[total];
        int idx = 0;
        for (int c = GLFW.GLFW_KEY_A; c <= GLFW.GLFW_KEY_Z; c++) keys[idx++] = c;
        for (int c = GLFW.GLFW_KEY_0; c <= GLFW.GLFW_KEY_9; c++) keys[idx++] = c;
        for (int e : extras) keys[idx++] = e;
        return keys;
    }
}
