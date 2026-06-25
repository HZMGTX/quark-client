package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

public class SpeedStep extends Module {

    private static final double DEFAULT_STEP = 0.6;

    private final DoubleSetting speed = register(new DoubleSetting(
            "Speed", "Step height when running", 0.5, 0.1, 2.0));

    public SpeedStep() {
        super("SpeedStep", "Step up blocks at running speed", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        setStepHeight(speed.get() + 0.5);
    }

    @Override
    public void onDisable() {
        setStepHeight(DEFAULT_STEP);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        setStepHeight(speed.get() + 0.5);
    }

    private void setStepHeight(double h) {
        if (mc.player == null) return;
        EntityAttributeInstance attr = mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(h);
    }
}
