package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class JumpReset extends Module {

    private final BoolSetting onlySprint = register(new BoolSetting(
            "Only Sprint", "Only perform jump reset while sprinting", true));

    private boolean doJump = false;

    public JumpReset() {
        super("JumpReset", "Jumps on attack to reset knockback and boost damage", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        doJump = false;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;

        if (onlySprint.isEnabled() && !mc.player.isSprinting()) return;

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
