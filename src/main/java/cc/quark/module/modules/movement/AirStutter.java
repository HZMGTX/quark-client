package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

public class AirStutter extends Module {

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Ticks between velocity jitter events", 3, 1, 10));
    private final DoubleSetting strength = register(new DoubleSetting(
            "Strength", "Jitter magnitude (blocks/tick)", 0.015, 0.002, 0.08));

    private final Random rng = new Random();
    private int ticker = 0;

    public AirStutter() {
        super("AirStutter", "Adds micro velocity noise in air to confuse prediction-based ACs", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) { ticker = 0; return; }

        if (++ticker < interval.get()) return;
        ticker = 0;

        Vec3d vel = mc.player.getVelocity();
        double str = strength.get();
        mc.player.setVelocity(
                vel.x + (rng.nextDouble() - 0.5) * str * 2,
                vel.y,
                vel.z + (rng.nextDouble() - 0.5) * str * 2);
    }
}
