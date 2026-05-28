package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

public class AntiAFK extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Action to perform to prevent AFK kick", "Rotate", "Rotate", "Jump", "Walk", "Swing"));

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Ticks between anti-AFK actions", 200, 20, 1200));

    private final BoolSetting chat = register(new BoolSetting(
            "Chat", "Send a chat message periodically", false));

    private final IntSetting chatInterval = register(new IntSetting(
            "Chat Interval", "Ticks between chat messages", 600, 200, 6000));

    private int tickCounter = 0;
    private int chatTickCounter = 0;
    private float yawOffset = 5f;
    private int walkTicks = 0;

    public AntiAFK() {
        super("AntiAFK", "Prevents AFK kick by periodically performing actions", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        chatTickCounter = 0;
        walkTicks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Chat sending
        if (chat.isEnabled()) {
            chatTickCounter++;
            if (chatTickCounter >= chatInterval.get()) {
                chatTickCounter = 0;
                if (mc.player.networkHandler != null) {
                    mc.player.networkHandler.sendChatMessage(".");
                }
            }
        }

        // Handle walk mode: continue moving for 3 ticks then stop
        if (walkTicks > 0) {
            walkTicks--;
            mc.options.forwardKey.setPressed(walkTicks > 0);
        }

        tickCounter++;
        if (tickCounter < interval.get()) return;
        tickCounter = 0;

        switch (mode.get()) {
            case "Rotate" -> {
                mc.player.setYaw(mc.player.getYaw() + yawOffset);
                yawOffset = -yawOffset;
            }
            case "Jump" -> {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
            case "Walk" -> {
                walkTicks = 3;
                mc.options.forwardKey.setPressed(true);
            }
            case "Swing" -> {
                mc.player.swingHand(Hand.MAIN_HAND);
                if (mc.player.networkHandler != null) {
                    mc.player.networkHandler.sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.forwardKey.setPressed(false);
        }
        walkTicks = 0;
    }
}
