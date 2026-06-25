package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.module.Category;
import cc.quark.module.Module;

public class AntiCrit extends Module {

    public AntiCrit() {
        super("AntiCrit", "Reduces incoming critical hits by cancelling elevated damage events", Category.COMBAT);
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        // If player is in air (crit condition for attacker) reduce the bonus crit damage
        if (!mc.player.isOnGround() && !mc.player.isInLava() && !mc.player.isSubmergedInWater()) {
            float reduced = event.getAmount() * 0.8f;
            event.setAmount(reduced);
        }
    }
}
