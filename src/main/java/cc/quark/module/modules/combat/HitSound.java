package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.sound.SoundEvents;

public class HitSound extends Module {

    private final ModeSetting sound = register(new ModeSetting("Sound", "Sound to play on hit", "Click", "Click", "Ding", "Pop"));
    private final DoubleSetting volume = register(new DoubleSetting("Volume", "Sound volume", 1.0, 0.1, 2.0));

    public HitSound() {
        super("HitSound", "Plays a custom sound on hit", Category.COMBAT);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || mc.world == null) return;

        float vol = (float) volume.get();
        switch (sound.get()) {
            case "Click" -> mc.world.playSound(mc.player,
                    mc.player.getBlockPos(),
                    SoundEvents.UI_BUTTON_CLICK.value(),
                    mc.player.getSoundCategory(),
                    vol, 1.0f);
            case "Ding" -> mc.world.playSound(mc.player,
                    mc.player.getBlockPos(),
                    SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(),
                    mc.player.getSoundCategory(),
                    vol, 2.0f);
            case "Pop" -> mc.world.playSound(mc.player,
                    mc.player.getBlockPos(),
                    SoundEvents.ENTITY_CHICKEN_EGG,
                    mc.player.getSoundCategory(),
                    vol, 1.2f);
        }
    }
}
