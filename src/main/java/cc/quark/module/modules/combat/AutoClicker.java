package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoClicker extends Module {

    private final DoubleSetting minCps = register(new DoubleSetting(
            "Min CPS", "Minimum clicks per second", 10.0, 1.0, 20.0));

    private final DoubleSetting maxCps = register(new DoubleSetting(
            "Max CPS", "Maximum clicks per second", 14.0, 1.0, 20.0));

    private final BoolSetting leftClick = register(new BoolSetting(
            "Left Click", "Auto left-click (attack)", true));

    private final BoolSetting rightClick = register(new BoolSetting(
            "Right Click", "Auto right-click (use item/block)", false));

    private final BoolSetting breakBlocks = register(new BoolSetting(
            "Break Blocks", "Also left-click to break blocks when looking at one", false));

    private final BoolSetting onlyHeld = register(new BoolSetting(
            "Only When Held", "Only click while holding the attack key", true));

    private long lastClick = 0;
    private long currentDelay = 100;

    public AutoClicker() {
        super("AutoClicker", "Automatically clicks at a randomized CPS", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastClick = 0;
        currentDelay = computeDelay();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (onlyHeld.isEnabled() && !mc.options.attackKey.isPressed()) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < currentDelay) return;

        lastClick = now;
        currentDelay = computeDelay();

        if (leftClick.isEnabled() && mc.crosshairTarget != null) {
            if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                EntityHitResult ehr = (EntityHitResult) mc.crosshairTarget;
                mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
                mc.player.swingHand(Hand.MAIN_HAND);
            } else if (breakBlocks.isEnabled() && mc.crosshairTarget.getType() == HitResult.Type.BLOCK) {
                // Trigger block break interaction
                BlockHitResult bhr = (BlockHitResult) mc.crosshairTarget;
                mc.interactionManager.attackBlock(bhr.getBlockPos(), bhr.getSide());
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }

        if (rightClick.isEnabled()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }

    private long computeDelay() {
        double min = minCps.get();
        double max = maxCps.get();
        if (min > max) min = max;
        double randomCps = min + Math.random() * (max - min);
        return (long) (1000.0 / randomCps);
    }
}
