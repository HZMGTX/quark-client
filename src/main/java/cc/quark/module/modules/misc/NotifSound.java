package cc.quark.module.modules.misc;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.sound.SoundEvents;

public class NotifSound extends Module {

    private final ModeSetting soundType = register(new ModeSetting(
            "Sound", "Sound to play on module toggle",
            "Click", "Click", "Ding", "Levelup", "Orb", "Note"));

    private final DoubleSetting volume = register(new DoubleSetting(
            "Volume", "Playback volume", 0.5, 0.0, 1.0));

    private final DoubleSetting pitch = register(new DoubleSetting(
            "Pitch", "Playback pitch", 1.0, 0.5, 2.0));

    public NotifSound() {
        super("NotifSound", "Plays a sound when any module is enabled or disabled", Category.MISC);
    }

    @Override
    public void onEnable() {
        playNotifSound(true);
    }

    @Override
    public void onDisable() {
        playNotifSound(false);
    }

    private void playNotifSound(boolean enabling) {
        if (mc.player == null || mc.world == null) return;

        net.minecraft.sound.SoundEvent sound = switch (soundType.get()) {
            case "Click"   -> SoundEvents.UI_BUTTON_CLICK.value();
            case "Ding"    -> SoundEvents.BLOCK_NOTE_BLOCK_PLING.value();
            case "Levelup" -> SoundEvents.ENTITY_PLAYER_LEVELUP;
            case "Orb"     -> SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP;
            case "Note"    -> SoundEvents.BLOCK_NOTE_BLOCK_HARP.value();
            default        -> SoundEvents.UI_BUTTON_CLICK.value();
        };

        float vol = (float) volume.get();
        float pit = (float) (enabling ? pitch.get() : pitch.get() * 0.8);

        mc.player.playSound(sound, vol, pit);
    }
}
