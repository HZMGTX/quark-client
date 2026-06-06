package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class StepClimb extends Module {

    private static final float VANILLA_STEP = 0.6f;

    private final DoubleSetting maxHeight = register(new DoubleSetting(
            "Max Height", "Maximum block height to step up automatically (blocks)", 2.0, 1.0, 5.0));

    private final BoolSetting dynamicRestore = register(new BoolSetting(
            "Dynamic Restore", "Restore vanilla step height one tick after stepping", true));

    private final BoolSetting onlyGround = register(new BoolSetting(
            "Only Ground", "Only apply increased step height while on the ground", true));

    private boolean wasApplied = false;

    public StepClimb() {
        super("StepClimb", "Automatically steps up any block height", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasApplied = false;
        if (mc.player != null) applyHeight();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) setStepHeight(VANILLA_STEP);
        wasApplied = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        boolean onGround = mc.player.isOnGround();

        if (onlyGround.isEnabled() && !onGround) {
            if (wasApplied) {
                setStepHeight(VANILLA_STEP);
                wasApplied = false;
            }
            return;
        }

        if (dynamicRestore.isEnabled()) {
            boolean collision = mc.player.horizontalCollision;
            if (collision) {
                applyHeight();
                wasApplied = true;
            } else if (wasApplied) {
                setStepHeight(VANILLA_STEP);
                wasApplied = false;
            }
        } else {
            applyHeight();
        }
    }

    private void applyHeight() {
        setStepHeight((float) maxHeight.get());
    }

    private void setStepHeight(float h) {
        net.minecraft.entity.attribute.EntityAttributeInstance attr =
                mc.player.getAttributeInstance(
                        net.minecraft.entity.attribute.EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(h);
    }
}
