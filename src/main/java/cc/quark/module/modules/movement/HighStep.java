package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

public class HighStep extends Module {

    private static final float VANILLA_STEP = 0.6f;

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Maximum step height in blocks", 1.0, 0.6, 2.0));

    public HighStep() {
        super("HighStep", "Allows stepping up blocks higher than 0.6 (up to 2 blocks)", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        applyStep();
    }

    @Override
    public void onDisable() {
        if (mc == null || mc.player == null) return;
        setStep(VANILLA_STEP);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc == null || mc.player == null) return;
        applyStep();
    }

    private void applyStep() {
        if (mc == null || mc.player == null) return;
        setStep((float) height.get());
    }

    private void setStep(float h) {
        EntityAttributeInstance attr =
                mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(h);
    }
}
