package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.math.BlockPos;

public class SpeedStep extends Module {

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Step height when running", 0.5, 0.1, 2.0));

    public SpeedStep() {
        super("SpeedStep", "Step up blocks at running speed", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) mc.player.stepHeight = (float) speed.get() + 0.5f;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.stepHeight = 0.6f;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        mc.player.stepHeight = (float) speed.get() + 0.5f;
    }
}
