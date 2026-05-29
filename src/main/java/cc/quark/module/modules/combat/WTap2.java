package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;

/**
 * WTap2 - improved WTap module.
 *
 * Sequence on every qualifying attack:
 *   1. Stop sprinting + release W key (breaks sprint server-side).
 *   2. Wait {@code releaseTicks} ticks.
 *   3. Press W key again + re-enable sprint.
 *
 * Includes timing verification: the module only performs the tap when the attack
 * cooldown was at the configured threshold, ensuring full damage.
 */
public class WTap2 extends Module {

    private final DoubleSetting cooldownPct = register(new DoubleSetting(
            "Cooldown %", "Minimum attack cooldown percentage required to W-tap", 90.0, 50.0, 100.0));

    private final IntSetting releaseTicks = register(new IntSetting(
            "Release Ticks", "Ticks to hold W released before re-pressing", 1, 1, 5));

    private final BoolSetting reSprint = register(new BoolSetting(
            "Re-Sprint", "Re-enable sprint after the W-tap delay", true));

    private final BoolSetting stopSprint = register(new BoolSetting(
            "Stop Sprint", "Call setSprinting(false) to reset server-side sprint state", true));

    private final BoolSetting requireForward = register(new BoolSetting(
            "Require Forward", "Only W-tap when actively moving forward", true));

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "Only On Ground", "Only W-tap when the player is on the ground", false));

    // State
    private int tapTicksRemaining = 0;
    private boolean wasMovingForward = false;

    public WTap2() {
        super("WTap2", "Improved sprint-reset on attack for maximised knockback", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.player != null && tapTicksRemaining > 0) {
            mc.options.forwardKey.setPressed(false);
            tapTicksRemaining = 0;
        }
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;

        // Only tap when moving forward (if setting enabled)
        if (requireForward.isEnabled() && mc.player.input.movementForward <= 0) return;

        // Ground check
        if (onlyOnGround.isEnabled() && !mc.player.isOnGround()) return;

        // Cooldown threshold check
        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < (cooldownPct.get() / 100.0f)) return;

        // Don't start a new tap if one is already in progress
        if (tapTicksRemaining > 0) return;

        wasMovingForward = mc.player.input.movementForward > 0;

        // Release sprint and W key
        if (stopSprint.isEnabled()) mc.player.setSprinting(false);
        mc.options.forwardKey.setPressed(false);

        tapTicksRemaining = releaseTicks.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || tapTicksRemaining <= 0) return;

        tapTicksRemaining--;
        if (tapTicksRemaining == 0) {
            // Restore W key if the player was moving forward
            if (wasMovingForward) {
                mc.options.forwardKey.setPressed(true);
            }
            if (reSprint.isEnabled()) {
                mc.player.setSprinting(true);
            }
        }
    }
}
