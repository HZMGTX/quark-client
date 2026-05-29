package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

/**
 * SprintReset - on EventAttack, release forward for 1 tick then re-press
 * (W-tap) to reset sprint, matching the NCP w-tap sprint reset technique.
 */
public class SprintReset extends Module {

    private int resetTimer = 0;

    public SprintReset() {
        super("SprintReset", "W-tap on attack to reset sprint for maximum knockback", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        resetTimer = 0;
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (!mc.player.isSprinting()) return;
        mc.player.setSprinting(false);
        resetTimer = 2; // unsprint for 2 ticks then re-sprint
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (resetTimer <= 0) return;

        resetTimer--;
        if (resetTimer == 0) {
            // Re-enable sprint
            mc.player.setSprinting(true);
        }
    }
}
