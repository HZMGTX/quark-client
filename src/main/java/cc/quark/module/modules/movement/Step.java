package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class Step extends Module {

    private static final float VANILLA_STEP = 0.6f;

    private final DoubleSetting height = register(new DoubleSetting(
            "Height", "Maximum block height to step up without jumping (blocks)", 1.0, 1.0, 2.5));

    private final BoolSetting onlyUpward = register(new BoolSetting(
            "Only Upward", "Only increase step height, never go below vanilla", true));

    private final BoolSetting smoothStep = register(new BoolSetting(
            "Smooth Step", "Gradually raise then lower step height over 3 ticks for natural look", false));

    private int   smoothPhase   = 0;
    private float smoothCurrent = VANILLA_STEP;
    private int   phaseTicks    = 0;

    public Step() {
        super("Step", "Steps up blocks automatically without jumping", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        smoothPhase   = 0;
        smoothCurrent = VANILLA_STEP;
        phaseTicks    = 0;
        if (mc.player != null) applyStepHeight();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) setStepHeight(VANILLA_STEP);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (smoothStep.isEnabled()) tickSmoothStep();
        else applyStepHeight();
    }

    private void applyStepHeight() {
        float desired = (float) height.get();
        setStepHeight(onlyUpward.isEnabled() ? Math.max(VANILLA_STEP, desired) : desired);
    }

    private void tickSmoothStep() {
        float target = (float) height.get();
        boolean aboutToStep = mc.player.horizontalCollision && mc.player.isOnGround();
        if (aboutToStep && smoothPhase == 0) { smoothPhase = 1; phaseTicks = 0; }

        switch (smoothPhase) {
            case 1 -> { smoothCurrent = VANILLA_STEP + (target - VANILLA_STEP) * 0.5f; setStepHeight(smoothCurrent); smoothPhase = 2; }
            case 2 -> { smoothCurrent = target; setStepHeight(smoothCurrent); smoothPhase = 3; }
            case 3 -> { setStepHeight(VANILLA_STEP); smoothCurrent = VANILLA_STEP; smoothPhase = 0; }
            default -> setStepHeight(onlyUpward.isEnabled() ? VANILLA_STEP : (float) height.get());
        }
    }

    private void setStepHeight(float h) {
        //? if mc >= "1.20.5" {
        net.minecraft.entity.attribute.EntityAttributeInstance attr =
                mc.player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(h);
        //?} else {
        /*mc.player.stepHeight = h;*/
        //?}
    }
}
