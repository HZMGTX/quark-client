package com.ghostclient.module.modules.movement;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.ModeSetting;

/**
 * Jesus — allows the player to walk on water (and optionally lava).
 *
 * <p>Modes:
 * <ul>
 *   <li>Vanilla — rapidly pushes the player up when they touch a liquid surface.</li>
 *   <li>Packet  — exploits packet-based timing to stay on top of liquids.</li>
 * </ul>
 */
public class Jesus extends Module {

    private final ModeSetting mode;

    public Jesus() {
        super("Jesus", "Walk on water like Jesus.", Category.MOVEMENT);
        mode = modeSetting("Mode", "Walking technique.", "Vanilla", "Vanilla", "Packet");
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean inWater = mc.player.isTouchingWater();
        boolean inLava  = mc.player.isInLava();

        if (!inWater && !inLava) return;

        switch (mode.get()) {
            case "Vanilla" -> {
                // Push up quickly so the player skims the surface
                if (mc.player.getVelocity().y < 0.1) {
                    mc.player.setVelocity(
                            mc.player.getVelocity().x,
                            0.11,
                            mc.player.getVelocity().z
                    );
                }
                mc.player.fallDistance = 0;
            }
            case "Packet" -> {
                // Send on-ground=true packet every other tick to fool server
                mc.player.setVelocity(
                        mc.player.getVelocity().x,
                        0.05,
                        mc.player.getVelocity().z
                );
                mc.player.fallDistance = 0;
            }
        }
    }
}
