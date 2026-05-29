package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;

public class AutoSprint extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "When to sprint",
            "Always", "Always", "Moving", "Forward"));

    public AutoSprint() {
        super("AutoSprint", "Automatically sprints based on movement mode", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setSprinting(false);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isSneaking()) return;
        if (mc.player.getHealth() <= 6f) return; // don't sprint when very low health (hunger cost)

        boolean shouldSprint = switch (mode.get()) {
            case "Always"   -> true;
            case "Moving"   -> mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0;
            case "Forward"  -> mc.player.input.movementForward > 0;
            default         -> false;
        };

        if (shouldSprint) {
            mc.player.setSprinting(true);
        }
    }
}
