package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

public class WTapPlus extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks to hold W released before re-pressing (1–5)", 2, 1, 5));

    private int releaseTicks = 0;
    private boolean wasMovingForward = false;

    public WTapPlus() {
        super("WTapPlus", "Enhanced W-tap: releases and re-presses W at attack time for maximum knockback", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (wasMovingForward && mc.player != null) {
            mc.options.forwardKey.setPressed(true);
        }
        releaseTicks = 0;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        // Only tap when actually moving forward and sprint is active
        if (mc.player.input.movementForward <= 0) return;
        if (!mc.player.isSprinting()) return;

        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < 0.9f) return;

        wasMovingForward = true;
        mc.player.setSprinting(false);
        mc.options.forwardKey.setPressed(false);
        releaseTicks = delay.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (releaseTicks > 0 && --releaseTicks == 0) {
            if (wasMovingForward) {
                mc.options.forwardKey.setPressed(true);
                mc.player.setSprinting(true);
            }
        }
    }
}
