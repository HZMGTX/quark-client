package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;

/**
 * AirStall - Stalls the player in mid-air by zeroing or reducing vertical
 * velocity. Useful for peeking over blocks or hovering briefly.
 */
public class AirStall extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "How to stall in mid-air",
            "Zero Y", "Zero Y", "Slow Fall", "Hover"));

    private final DoubleSetting slowFactor = register(new DoubleSetting(
            "Slow Factor", "Y-velocity multiplier in Slow Fall mode (0 = full stop)", 0.2, 0.0, 0.9));

    private final DoubleSetting hoverY = register(new DoubleSetting(
            "Hover Y", "Y-velocity used in Hover mode (negative = tiny drop)", -0.05, -0.3, 0.0));

    private final BoolSetting onlyWhenHeld = register(new BoolSetting(
            "Only When Held", "Only stall while holding the sneak key", false));

    public AirStall() {
        super("AirStall", "Stalls in mid-air by zeroing or reducing vertical velocity", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mc.player.isOnGround()) return;
        if (onlyWhenHeld.isEnabled() && !mc.options.sneakKey.isPressed()) return;

        switch (mode.get()) {
            case "Zero Y" -> mc.player.setVelocity(
                    mc.player.getVelocity().x,
                    0.0,
                    mc.player.getVelocity().z);
            case "Slow Fall" -> mc.player.setVelocity(
                    mc.player.getVelocity().x,
                    mc.player.getVelocity().y * slowFactor.get(),
                    mc.player.getVelocity().z);
            case "Hover" -> {
                double curY = mc.player.getVelocity().y;
                if (curY < hoverY.get()) {
                    mc.player.setVelocity(
                            mc.player.getVelocity().x,
                            hoverY.get(),
                            mc.player.getVelocity().z);
                }
            }
        }
    }
}
