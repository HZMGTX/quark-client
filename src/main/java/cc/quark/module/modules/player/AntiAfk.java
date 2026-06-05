package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;

public class AntiAfk extends Module {

    private final IntSetting interval = register(new IntSetting(
            "Interval", "Ticks between actions (20 = 1s)", 100, 20, 1200));
    private final ModeSetting action = register(new ModeSetting(
            "Action", "Anti-AFK action to perform", "Rotate", "Rotate", "Jump", "Sneak", "Swing"));
    private final BoolSetting randomize = register(new BoolSetting(
            "Randomize", "Slightly randomize timing to avoid detection", true));

    private int tickCounter = 0;
    private float yawOffset  = 0f;

    public AntiAfk() {
        super("AntiAfk", "Prevents AFK kick by performing periodic actions", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        int threshold = interval.get() + (randomize.isEnabled() ? (int)(Math.random() * 20) - 10 : 0);
        if (++tickCounter < threshold) return;
        tickCounter = 0;

        switch (action.get()) {
            case "Rotate" -> {
                yawOffset = (yawOffset + 15f) % 360f;
                mc.player.setYaw(mc.player.getYaw() + yawOffset);
            }
            case "Jump" -> {
                if (mc.player.isOnGround()) mc.player.jump();
            }
            case "Sneak" -> {
                mc.player.setSneaking(!mc.player.isSneaking());
            }
            case "Swing" -> {
                mc.player.swingMainHand();
            }
        }
    }
}
