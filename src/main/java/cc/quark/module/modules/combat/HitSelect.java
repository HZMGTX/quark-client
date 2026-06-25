package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class HitSelect extends Module {

    private final BoolSetting requireSprint = register(new BoolSetting(
            "Require Sprint", "Only attack while sprinting (guarantees knockback bonus)", true));

    private final BoolSetting requireFall = register(new BoolSetting(
            "Require Fall", "Only attack while falling (guarantees critical hit)", true));

    public HitSelect() {
        super("HitSelect", "Only attacks when sprint and fall state guarantee a critical hit", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;

        if (requireSprint.isEnabled() && !mc.player.isSprinting()) {
            event.cancel();
            return;
        }

        // A crit requires: falling (negative or zero vertical velocity), not on ground,
        // not in water, not riding, and attack cooldown fully charged.
        if (requireFall.isEnabled()) {
            boolean falling = mc.player.getVelocity().y < 0.0
                    && !mc.player.isOnGround()
                    && !mc.player.isTouchingWater()
                    && !mc.player.isInLava()
                    && mc.player.getVehicle() == null;
            if (!falling) {
                event.cancel();
                return;
            }
        }

        // Cooldown must be fully charged for the attack to deal full damage
        if (mc.player.getAttackCooldownProgress(0.5f) < 1.0f) {
            event.cancel();
        }
    }
}
