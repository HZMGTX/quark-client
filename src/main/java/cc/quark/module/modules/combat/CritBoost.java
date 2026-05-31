package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class CritBoost extends Module {

    private final BoolSetting onlyOnGround = register(new BoolSetting(
            "OnlyOnGround", "Only boost when actually on the ground between jumps", false));

    private boolean wasInAir = false;

    public CritBoost() {
        super("CritBoost", "Increases crit damage multiplier client-side by timing attacks at peak fall", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        wasInAir = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean onGround = mc.player.isOnGround();
        double vy = mc.player.getVelocity().y;

        if (onlyOnGround.isEnabled()) {
            if (!onGround) return;
        }

        if (!wasInAir && !onGround) {
            wasInAir = true;
        }

        if (wasInAir && onGround) {
            wasInAir = false;
            return;
        }

        if (!onGround && vy < 0 && vy > -0.2) {
            mc.player.setVelocity(mc.player.getVelocity().x, -0.1, mc.player.getVelocity().z);
        }
    }
}
