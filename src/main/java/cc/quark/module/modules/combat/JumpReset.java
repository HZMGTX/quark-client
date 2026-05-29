package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

/**
 * JumpReset — jumps the moment an attack lands so sprint knockback bonus is
 * preserved and a natural crit window opens on the next swing.
 * Only triggers while sprinting (optional) and when the attack CD is high.
 */
public class JumpReset extends Module {

    private final DoubleSetting cooldownPct   = register(new DoubleSetting(
            "Cooldown %",    "Min attack CD % before jump-reset activates", 90.0, 50.0, 100.0));
    private final BoolSetting   onlySprinting = register(new BoolSetting(
            "Only Sprinting","Only jump-reset while sprinting",             true));
    private final BoolSetting   reSprint      = register(new BoolSetting(
            "Re-Sprint",     "Set sprinting=true again after the jump",     true));

    private boolean doJump = false;

    public JumpReset() {
        super("JumpReset", "Jumps on attack to preserve sprint knockback and open crit windows", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        doJump = false;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (onlySprinting.isEnabled() && !mc.player.isSprinting()) return;
        if (mc.player.getAttackCooldownProgress(0f) < (float) (cooldownPct.get() / 100.0)) return;
        if (mc.player.isOnGround()) {
            doJump = true;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (doJump && mc.player.isOnGround()) {
            mc.player.jump();
            if (reSprint.isEnabled()) mc.player.setSprinting(true);
            doJump = false;
        }
    }
}
