package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

public class HealthAlert extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting(
            "Threshold", "Alert when health drops below this (hearts)", 6.0, 1.0, 15.0));
    private final BoolSetting playSound = register(new BoolSetting(
            "Sound", "Play alert sound", true));
    private final BoolSetting chatAlert = register(new BoolSetting(
            "Chat Alert", "Show alert in chat", false));
    private final BoolSetting screenTitle = register(new BoolSetting(
            "Screen Title", "Show title overlay", false));

    private final TimerUtil cooldown = new TimerUtil();
    private boolean wasLow = false;

    public HealthAlert() {
        super("HealthAlert", "Alerts when health drops below a configurable threshold", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        wasLow = false;
        cooldown.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        // Health is stored in half-hearts; threshold is in full hearts
        float hp = mc.player.getHealth() / 2f;
        boolean isLow = hp <= threshold.get();

        if (isLow && !wasLow && cooldown.hasReached(3000)) {
            String msg = String.format("Low HP: %.1f / %.1f hearts!", hp, mc.player.getMaxHealth() / 2f);

            NotificationOverlay.send("Health Alert", msg, NotificationOverlay.NotifType.WARNING);

            if (chatAlert.isEnabled()) {
                ChatUtil.warn(msg);
            }
            if (playSound.isEnabled() && mc.getSoundManager() != null) {
                mc.getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1.5f, 1.0f));
            }
            if (screenTitle.isEnabled()) {
                mc.inGameHud.setTitle(net.minecraft.text.Text.literal(
                        "§c⚠ LOW HEALTH ⚠"));
                mc.inGameHud.setSubtitle(net.minecraft.text.Text.literal(
                        "§e" + String.format("%.1f", hp) + " hearts"));
            }
            cooldown.reset();
        }
        wasLow = isLow;
    }
}
