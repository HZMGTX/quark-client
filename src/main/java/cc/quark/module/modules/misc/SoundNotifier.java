package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;

public class SoundNotifier extends Module {
    private final BoolSetting playerSeen = register(new BoolSetting("Player Nearby", "Play sound when player nearby", true));
    private int playerCount = 0;

    public SoundNotifier() { super("SoundNotifier", "Plays sounds for important game events", Category.MISC); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        int count = 0;
        for (var ent : mc.world.getEntities()) {
            if (ent instanceof PlayerEntity pe && pe != mc.player && mc.player.distanceTo(pe) < 20) count++;
        }
        if (playerSeen.isEnabled() && count > playerCount && count > 0) {
            mc.world.playSound(mc.player, mc.player.getBlockPos(),
                net.minecraft.sound.SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                SoundCategory.MASTER, 1.0f, 1.0f);
        }
        playerCount = count;
    }
}
