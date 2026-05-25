package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.util.Hand;

public class AntiFireball extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Auto-deflect range in blocks", 3.0, 1.0, 6.0));

    public AntiFireball() {
        super("AntiFireball", "Automatically deflects incoming fireballs and wither skulls", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof AbstractFireballEntity)) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }
}
