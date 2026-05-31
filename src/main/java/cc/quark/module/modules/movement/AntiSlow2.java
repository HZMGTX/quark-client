package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.util.math.Vec3d;

public class AntiSlow2 extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How to handle item-use slowdown", "Cancel", "Cancel", "Reduce"));

    public AntiSlow2() {
        super("AntiSlow2", "Cancels speed reduction from using items like food, bow, or sword", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!mc.player.isUsingItem()) return;

        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        if (!moving) return;

        Vec3d vel = mc.player.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hSpeed < 0.01) return;

        if (mode.get().equals("Cancel")) {
            // Restore to normal sprint speed (~0.29 blocks/tick)
            double target = 0.29;
            double scale = target / hSpeed;
            mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
        } else {
            // Reduce: partially restore, less aggressive
            double scale = 1.3;
            mc.player.setVelocity(vel.x * scale, vel.y, vel.z * scale);
        }
    }
}
