package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;

public class KeepSprint extends Module {

    public KeepSprint() {
        super("KeepSprint", "Maintains sprint momentum after attacking — prevents 0-tick sprint loss", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking() || mc.player.isUsingItem()) return;
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;

        boolean moving = mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
        if (moving && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
