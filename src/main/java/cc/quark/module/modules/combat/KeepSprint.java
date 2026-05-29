package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * KeepSprint — prevents sprint from being reset after attacking.
 * Also re-sprints via EventAttack so the flag is restored in the same tick
 * it would normally be cleared.
 */
public class KeepSprint extends Module {

    private final BoolSetting hungerCheck = register(new BoolSetting(
            "Hunger Check", "Only sprint when food level > 6", true));

    public KeepSprint() {
        super("KeepSprint", "Maintains sprint after attacking — prevents 0-tick sprint loss", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (hungerCheck.isEnabled() && mc.player.getHungerManager().getFoodLevel() <= 6) return;
        mc.player.setSprinting(true);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking() || mc.player.isUsingItem()) return;
        if (hungerCheck.isEnabled() && mc.player.getHungerManager().getFoodLevel() <= 6) return;

        boolean moving = mc.player.input.movementForward > 0 || mc.player.input.movementSideways != 0;
        if (moving && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
