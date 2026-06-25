package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class FoodAlert extends Module {

    private final IntSetting threshold = register(new IntSetting(
            "Threshold", "Alert when food drops below this level", 8, 1, 20));
    private final IntSetting cooldownSec = register(new IntSetting(
            "CooldownSec", "Seconds between alerts", 30, 5, 120));
    private final BoolSetting sound = register(new BoolSetting(
            "Sound", "Play a sound when food is low", false));

    private final TimerUtil timer = new TimerUtil();

    public FoodAlert() {
        super("FoodAlert", "Alerts when food level drops below a threshold", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        int foodLevel = mc.player.getHungerManager().getFoodLevel();
        if (foodLevel < threshold.get() && timer.hasReached(cooldownSec.get() * 1000L)) {
            ChatUtil.warn("[FoodAlert] Food is low: " + foodLevel + "/20");
            if (sound.isEnabled() && mc.getSoundManager() != null) {
                mc.getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.0f));
            }
            timer.reset();
        }
    }
}
