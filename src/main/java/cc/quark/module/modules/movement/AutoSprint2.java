package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class AutoSprint2 extends Module {

    private final BoolSetting stopOnHit = register(new BoolSetting("StopOnHit", "Stop sprinting when receiving damage",  true));
    private final BoolSetting stopOnEat = register(new BoolSetting("StopOnEat", "Stop sprinting while eating/drinking", true));

    public AutoSprint2() {
        super("AutoSprint2", "Enhanced auto-sprint with combat awareness", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Stop conditions
        if (stopOnHit.isEnabled() && mc.player.hurtTime > 0) {
            return;
        }
        if (stopOnEat.isEnabled() && mc.player.isUsingItem()) {
            mc.player.setSprinting(false);
            return;
        }

        // Sprint if moving forward
        if (mc.player.input.movementForward > 0 && !mc.player.isSneaking()) {
            mc.player.setSprinting(true);
        }
    }
}
