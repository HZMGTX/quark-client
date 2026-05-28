package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class HealthAlert extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting("Threshold", "Warn when health drops below this (hearts)", 6.0, 1.0, 15.0));
    private final BoolSetting sound = register(new BoolSetting("Sound", "Play ping sound on alert", true));

    private final TimerUtil cooldown = new TimerUtil();
    private boolean wasLow = false;

    public HealthAlert() {
        super("HealthAlert", "Warns when health is critically low", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        float hp = mc.player.getHealth() / 2f;
        boolean isLow = hp <= threshold.get();

        if (isLow && !wasLow && cooldown.hasReached(3000)) {
            NotificationOverlay.send("Health Alert", "Low HP: " + String.format("%.1f", hp) + " hearts!", NotificationOverlay.NotifType.WARNING);
            if (sound.isEnabled() && mc.getSoundManager() != null) {
                mc.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f));
            }
            cooldown.reset();
        }

        wasLow = isLow;
    }

    @Override
    public void onEnable() {
        wasLow = false;
        cooldown.reset();
    }
}
