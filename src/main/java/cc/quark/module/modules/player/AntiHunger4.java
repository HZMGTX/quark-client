package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class AntiHunger4 extends Module {

    private final BoolSetting noSprint = register(new BoolSetting(
            "NoSprintHunger", "Prevent hunger drain from sprinting", true));
    private final BoolSetting noJump = register(new BoolSetting(
            "NoJumpHunger", "Prevent exhaustion increase from jumping", true));

    public AntiHunger4() {
        super("AntiHunger4", "Sprint without hunger drain", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Reset exhaustion to 0 to prevent hunger loss
        if (noSprint.isEnabled() && mc.player.isSprinting()) {
            mc.player.getHungerManager().addExhaustion(-0.1f);
        }

        if (noJump.isEnabled() && mc.player.input.jumping) {
            mc.player.getHungerManager().addExhaustion(-0.05f);
        }
    }
}
