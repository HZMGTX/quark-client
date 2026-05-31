package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.Vec3d;

public class WaterBoost extends Module {

    private final DoubleSetting multiplier = register(new DoubleSetting(
            "Multiplier", "Speed multiplier while swimming", 2.0, 1.1, 5.0));

    private final BoolSetting lavaBoost = register(new BoolSetting(
            "LavaBoost", "Also boost speed in lava", false));

    public WaterBoost() {
        super("WaterBoost", "Increases swim speed in water", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean inWater = mc.player.isTouchingWater();
        boolean inLava = lavaBoost.isEnabled() && mc.player.isInLava();

        if (!inWater && !inLava) return;

        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        if (!moving) return;

        Vec3d vel = mc.player.getVelocity();
        double hSpeed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
        if (hSpeed < 0.001) return;

        double m = multiplier.get();
        mc.player.setVelocity(vel.x * m, vel.y, vel.z * m);
    }
}
