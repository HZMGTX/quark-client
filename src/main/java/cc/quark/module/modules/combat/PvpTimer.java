package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * PvpTimer - tracks how long an enemy player has been within combat range.
 */
public class PvpTimer extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Combat range", 6.0, 2.0, 16.0));

    private int combatTicks;

    public PvpTimer() {
        super("PvpTimer", "Counts time spent in combat range", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        combatTicks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        boolean inCombat = false;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(entity) <= range.get()) {
                inCombat = true;
                break;
            }
        }
        if (inCombat) {
            combatTicks++;
            if (combatTicks % 20 == 0) {
                ChatUtil.info("In combat for " + (combatTicks / 20) + "s");
            }
        } else {
            combatTicks = 0;
        }
    }
}
