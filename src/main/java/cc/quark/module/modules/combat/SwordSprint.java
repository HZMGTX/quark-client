package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;

/**
 * SwordSprint — performs a sprint-reset on attack by releasing forward movement
 * for a configurable number of ticks, then re-enabling it.
 * Provides better knockback than vanilla sprint attacks.
 */
public class SwordSprint extends Module {

    private final IntSetting releaseTicks = register(new IntSetting(
            "ReleaseTicks", "Ticks to release forward key after an attack", 2, 1, 5));

    private int resetTimer = 0;
    private boolean wasForward = false;

    public SwordSprint() {
        super("SwordSprint", "Sprint-resets on attack for improved knockback", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        resetTimer = 0;
        if (mc.player != null && wasForward) {
            mc.options.forwardKey.setPressed(false);
        }
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (!mc.player.isSprinting()) return;

        wasForward = mc.player.input.movementForward > 0;
        mc.player.setSprinting(false);
        mc.options.forwardKey.setPressed(false);
        resetTimer = releaseTicks.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (resetTimer <= 0) return;

        resetTimer--;
        if (resetTimer == 0) {
            if (wasForward) {
                mc.options.forwardKey.setPressed(true);
            }
            mc.player.setSprinting(true);
        }
    }
}
