package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

import java.util.Random;

public class Jesus extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Walking technique", "Vanilla", "Vanilla", "Packet"));

    private final BoolSetting lava = register(new BoolSetting(
            "Lava", "Also walk on lava", false));

    private final BoolSetting bob = register(new BoolSetting(
            "Bob", "Subtle bobbing animation on the surface", false));

    private int tickCounter = 0;
    private final Random random = new Random();

    public Jesus() {
        super("Jesus", "Walk on water like Jesus.", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean inWater = mc.player.isTouchingWater();
        boolean inLava = mc.player.isInLava();

        if (!inWater && !(lava.isEnabled() && inLava)) return;

        boolean sneaking = mc.options.sneakKey.isPressed();

        if (sneaking) return;

        tickCounter++;

        double bobOffset = 0.0;
        if (bob.isEnabled()) {
            bobOffset = Math.sin(tickCounter * 0.2) * 0.01;
        }

        switch (mode.get()) {
            case "Vanilla" -> {
                double vy = mc.player.getVelocity().y;
                if (vy < 0.1) {
                    mc.player.setVelocity(
                            mc.player.getVelocity().x,
                            0.11 + bobOffset,
                            mc.player.getVelocity().z);
                } else if (bob.isEnabled()) {
                    mc.player.setVelocity(
                            mc.player.getVelocity().x,
                            vy + bobOffset,
                            mc.player.getVelocity().z);
                }
                mc.player.fallDistance = 0;
            }

            case "Packet" -> {
                mc.player.setVelocity(
                        mc.player.getVelocity().x,
                        0.0 + bobOffset,
                        mc.player.getVelocity().z);
                mc.player.fallDistance = 0;
            }
        }
    }

    @Override
    public void onDisable() {
        tickCounter = 0;
    }
}
