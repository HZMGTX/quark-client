package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;

/**
 * Damage - cancels or modifies incoming damage events.
 *
 * <ul>
 *   <li><b>Fall</b> - cancel only fall damage (no attacker, no projectile).</li>
 *   <li><b>All</b>  - cancel all damage types.</li>
 * </ul>
 */
public class Damage extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Which damage to cancel", "Fall", "Fall", "All"));

    public Damage() {
        super("Damage", "Cancel fall damage and/or all damage", Category.MOVEMENT);
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;

        switch (mode.get()) {
            case "All" -> event.cancel();
            case "Fall" -> {
                // Fall damage has no attacker and no source entity
                boolean hasAttacker = event.getSource().getAttacker() != null;
                boolean hasSource   = event.getSource().getSource() != null;
                if (!hasAttacker && !hasSource) {
                    event.cancel();
                }
            }
        }
    }
}
