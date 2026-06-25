package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

public class ElytraControl extends Module {

    private final DoubleSetting sensitivity = register(new DoubleSetting(
            "Sensitivity", "Elytra control sensitivity multiplier", 1.0, 0.1, 3.0));

    private final BoolSetting autoLevel = register(new BoolSetting(
            "Auto Level", "Automatically level out when not pressing keys", true));

    public ElytraControl() {
        super("ElytraControl", "Fine-grained elytra flight control", Category.MOVEMENT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null) return;
        if (!mc.player.isFallFlying()) return;

        // Check if wearing elytra
        var chestStack = mc.player.getInventory().getArmorStack(2);
        if (chestStack.getItem() != Items.ELYTRA) return;

        float yaw = event.getYaw();
        float pitch = event.getPitch();

        // Apply sensitivity scaling
        float sens = (float) sensitivity.get();
        mc.player.setYaw(mc.player.getYaw() + (yaw - mc.player.getYaw()) * sens);

        if (autoLevel.isEnabled()) {
            Vec3d vel = mc.player.getVelocity();
            if (!mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed()) {
                // Gently level pitch toward 0
                float targetPitch = 0f;
                mc.player.setPitch(mc.player.getPitch() + (targetPitch - mc.player.getPitch()) * 0.1f);
            }
        }
    }
}
