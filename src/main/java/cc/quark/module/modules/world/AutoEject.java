package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;

/**
 * AutoEject - Automatically dismounts the player from vehicles.
 * Can be triggered by key code or automatically when the player takes damage.
 */
public class AutoEject extends Module {

    private final IntSetting keyCode = register(new IntSetting(
            "KeyCode", "Key code to trigger eject (0 = disabled)", 0, 0, 512));
    private final BoolSetting autoOnDamage = register(new BoolSetting(
            "AutoOnDamage", "Auto-eject when player takes damage", true));

    private float lastHealth = 20.0f;

    public AutoEject() {
        super("AutoEject", "Automatically ejects from vehicles on command", Category.WORLD);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) lastHealth = mc.player.getHealth();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean shouldEject = false;

        // Damage detection
        if (autoOnDamage.isEnabled()) {
            float currentHealth = mc.player.getHealth();
            if (currentHealth < lastHealth) {
                shouldEject = true;
            }
            lastHealth = currentHealth;
        }

        // Key-triggered eject
        if (keyCode.get() > 0) {
            try {
                if (org.lwjgl.glfw.GLFW.glfwGetKey(mc.getWindow().getHandle(), keyCode.get())
                        == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                    shouldEject = true;
                }
            } catch (Exception ignored) {}
        }

        if (shouldEject && mc.player.hasVehicle()) {
            mc.player.dismountVehicle();
        }
    }
}
