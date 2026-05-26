package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * HealthTags - periodically reports the health of nearby players.
 */
public class HealthTags extends Module {

    private final BoolSetting onlyLow = register(new BoolSetting("Only Low", "Only report low-health players", false));

    private int ticks;

    public HealthTags() {
        super("HealthTags", "Shows health of nearby players in chat", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        ticks++;
        if (ticks < 40) return;
        ticks = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof PlayerEntity player)) continue;
            if (onlyLow.isEnabled() && player.getHealth() > 10f) continue;
            ChatUtil.info(player.getGameProfile().getName() + ": " + (int) player.getHealth() + " HP");
        }
    }
}
