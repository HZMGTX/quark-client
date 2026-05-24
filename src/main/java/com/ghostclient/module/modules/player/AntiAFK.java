package com.ghostclient.module.modules.player;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventTick;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

import java.util.Random;

/**
 * AntiAFK - performs random actions periodically to prevent AFK kicks.
 */
public class AntiAFK extends Module {

    private final BoolSetting jump = register(new BoolSetting(
            "Jump", "Randomly jump", true));

    private final BoolSetting sneak = register(new BoolSetting(
            "Sneak", "Randomly toggle sneak", true));

    private final BoolSetting rotate = register(new BoolSetting(
            "Rotate", "Randomly rotate camera", true));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Swing hand randomly", false));

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Ticks between actions", 100, 20, 200));

    private int tickCounter = 0;
    private final Random random = new Random();

    // Saved yaw/pitch to restore after random rotate
    private float savedYaw;
    private float savedPitch;
    private boolean rotated = false;

    public AntiAFK() {
        super("AntiAFK", "Performs random actions to prevent AFK kick", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        rotated = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        tickCounter++;

        // Un-rotate one tick after rotating
        if (rotated) {
            mc.player.setYaw(savedYaw);
            mc.player.setPitch(savedPitch);
            rotated = false;
        }

        if (tickCounter < interval.get()) return;
        tickCounter = 0;

        // Pick a random action
        int action = random.nextInt(4);

        switch (action) {
            case 0 -> {
                if (jump.isEnabled() && mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
            case 1 -> {
                if (sneak.isEnabled()) {
                    // Send sneak press then release after 2 ticks
                    mc.player.networkHandler.sendPacket(
                            new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                    // Schedule release next interval (simplified: just toggle)
                    mc.player.networkHandler.sendPacket(
                            new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                }
            }
            case 2 -> {
                if (rotate.isEnabled()) {
                    savedYaw = mc.player.getYaw();
                    savedPitch = mc.player.getPitch();
                    float randomYaw = savedYaw + (random.nextFloat() * 60f - 30f);
                    float randomPitch = savedPitch + (random.nextFloat() * 30f - 15f);
                    randomPitch = Math.max(-89f, Math.min(89f, randomPitch));
                    mc.player.setYaw(randomYaw);
                    mc.player.setPitch(randomPitch);
                    rotated = true;
                }
            }
            case 3 -> {
                if (swing.isEnabled()) {
                    mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }
        }
    }
}
