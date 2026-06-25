package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;

/**
 * AntiAFK3 - Keeps the player active by randomly mixing movement actions
 * (rotate, jump, walk, arm-swing, sneak) to avoid AFK kick.
 */
public class AntiAFK3 extends Module {

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Seconds between anti-AFK actions", 20, 5, 120));
    private final BoolSetting doRotate = register(new BoolSetting(
            "Rotate", "Slightly rotate yaw", true));
    private final BoolSetting doJump = register(new BoolSetting(
            "Jump", "Jump occasionally", true));
    private final BoolSetting doWalk = register(new BoolSetting(
            "Walk", "Take a brief step forward", false));
    private final BoolSetting doSwing = register(new BoolSetting(
            "Swing", "Swing main hand", false));
    private final BoolSetting doSneak = register(new BoolSetting(
            "Sneak", "Briefly sneak then release", false));

    private final TimerUtil timer = new TimerUtil();
    private int actionIndex = 0;
    private int walkTicks = 0;
    private boolean sneaking = false;
    private int sneakTicks = 0;

    public AntiAFK3() {
        super("AntiAFK3", "Keeps player active with randomised actions to prevent AFK kick", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
        actionIndex = 0;
        walkTicks = 0;
        sneaking = false;
        sneakTicks = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.forwardKey.setPressed(false);
            if (sneaking && mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
            }
        }
        walkTicks = 0;
        sneaking = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        // Finish ongoing walk
        if (walkTicks > 0) {
            walkTicks--;
            mc.options.forwardKey.setPressed(walkTicks > 0);
        }

        // Release sneak after 2 ticks
        if (sneaking) {
            if (++sneakTicks >= 2) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                        mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                sneaking = false;
                sneakTicks = 0;
            }
            return;
        }

        if (!timer.hasReached(interval.get() * 1000L)) return;
        timer.reset();

        // Cycle through enabled actions in round-robin order
        int tried = 0;
        while (tried < 5) {
            actionIndex = (actionIndex + 1) % 5;
            tried++;
            switch (actionIndex) {
                case 0 -> {
                    if (doRotate.isEnabled()) {
                        mc.player.setYaw(mc.player.getYaw() + (mc.player.getYaw() % 10 == 0 ? 5f : -5f));
                        return;
                    }
                }
                case 1 -> {
                    if (doJump.isEnabled() && mc.player.isOnGround()) {
                        mc.player.jump();
                        return;
                    }
                }
                case 2 -> {
                    if (doWalk.isEnabled()) {
                        walkTicks = 3;
                        mc.options.forwardKey.setPressed(true);
                        return;
                    }
                }
                case 3 -> {
                    if (doSwing.isEnabled()) {
                        mc.player.swingHand(Hand.MAIN_HAND);
                        return;
                    }
                }
                case 4 -> {
                    if (doSneak.isEnabled()) {
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(
                                mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                        sneaking = true;
                        sneakTicks = 0;
                        return;
                    }
                }
            }
        }
    }
}
