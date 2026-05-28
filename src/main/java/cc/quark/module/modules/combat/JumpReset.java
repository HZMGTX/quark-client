package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class JumpReset extends Module {

    private final DoubleSetting cooldownPct = register(new DoubleSetting(
            "Cooldown %", "Minimum attack cooldown percentage required before jump-resetting", 90.0, 50.0, 100.0));

    private final BoolSetting onlySprinting = register(new BoolSetting(
            "Only Sprinting", "Only perform jump reset while sprinting", true));

    private boolean doJump = false;

    public JumpReset() {
        super("JumpReset", "Jumps on attack to reset sprint knockback and boost damage", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        doJump = false;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;

        // Only jump reset when sprinting if enabled
        if (onlySprinting.isEnabled() && !mc.player.isSprinting()) return;

        // Only jump reset when cooldown is high enough
        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < (cooldownPct.get() / 100.0f)) return;

        if (mc.player.isOnGround()) {
            doJump = true;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (doJump && mc.player.isOnGround()) {
            mc.player.jump();
            doJump = false;
        }
    }
}
