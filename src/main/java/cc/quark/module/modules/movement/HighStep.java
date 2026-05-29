package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

/**
 * HighStep - allows stepping up blocks higher than vanilla (up to 2.5 blocks)
 * by increasing the player's step height attribute each tick.
 */
public class HighStep extends Module {

    private static final float VANILLA_STEP = 0.6f;

    private final DoubleSetting stepHeight = register(new DoubleSetting(
            "Step Height", "Maximum step height in blocks", 2.0, 1.0, 2.5));

    public HighStep() {
        super("HighStep", "Step up blocks higher than vanilla (up to 2.5 blocks)", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        applyStepHeight();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        setStepHeight(VANILLA_STEP);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        applyStepHeight();
    }

    private void applyStepHeight() {
        if (mc.player == null) return;
        setStepHeight((float) stepHeight.get());
    }

    private void setStepHeight(float h) {
        EntityAttributeInstance attr =
                mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(h);
    }
}
