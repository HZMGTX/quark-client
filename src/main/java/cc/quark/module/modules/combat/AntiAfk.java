package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.util.Hand;

/**
 * AntiAfk (Combat) - Keeps the player active in combat-zone AFK scenarios by
 * randomly rotating and/or jumping at a configurable interval. Unlike the
 * general AntiAFK module, this one includes random yaw variance so it looks
 * more natural when used near enemies or near-combat automation.
 */
public class AntiAfk extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Action used to prevent AFK kick",
            "Rotate", "Rotate", "Jump", "Both", "Swing"));

    private final IntSetting minInterval = register(new IntSetting(
            "Min Interval", "Minimum ticks between AFK actions", 100, 20, 1000));

    private final IntSetting maxInterval = register(new IntSetting(
            "Max Interval", "Maximum ticks between AFK actions", 300, 20, 2000));

    private final DoubleSetting rotateAmount = register(new DoubleSetting(
            "Rotate Amount", "Yaw change per action (degrees)", 15.0, 5.0, 180.0));

    private final BoolSetting randomRotate = register(new BoolSetting(
            "Random Rotate", "Randomize rotation direction and amount", true));

    private int tickCounter = 0;
    private int nextInterval = 200;

    public AntiAfk() {
        super("AntiAfk", "Combat anti-AFK: randomly rotates/jumps every N seconds", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        nextInterval = computeInterval();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        tickCounter++;
        if (tickCounter < nextInterval) return;
        tickCounter = 0;
        nextInterval = computeInterval();

        switch (mode.get()) {
            case "Rotate" -> doRotate();
            case "Jump" -> doJump();
            case "Both" -> {
                doRotate();
                doJump();
            }
            case "Swing" -> {
                mc.player.swingHand(Hand.MAIN_HAND);
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                }
            }
        }
    }

    private void doRotate() {
        float delta;
        if (randomRotate.isEnabled()) {
            double base = rotateAmount.get();
            delta = (float)(Math.random() * base * 2 - base);
        } else {
            delta = (float) rotateAmount.get();
        }
        mc.player.setYaw(mc.player.getYaw() + delta);
    }

    private void doJump() {
        if (mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    private int computeInterval() {
        int min = minInterval.get();
        int max = Math.max(min, maxInterval.get());
        return min + (int)(Math.random() * (max - min + 1));
    }
}
