package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class SprintReset extends Module {

    private final DoubleSetting cooldown = register(new DoubleSetting(
            "Cooldown", "Attack cooldown percentage (0.0-1.0) at which to reset sprint for 1.9+ combat", 0.9, 0.0, 1.0));

    public SprintReset() {
        super("SprintReset", "Resets sprint on exact attack timing", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isSprinting()) return;

        float attackCooldown = mc.player.getAttackCooldownProgress(0f);
        if (attackCooldown >= (float) cooldown.get()) {
            // Briefly stop sprint then re-enable for next hit timing
            mc.player.setSprinting(false);
            // Re-enable on next tick if moving
            if (mc.player.input != null && (mc.player.input.movementForward > 0 || mc.player.input.movementSideways != 0)) {
                mc.player.setSprinting(true);
            }
        }
    }
}
