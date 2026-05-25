package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;

public class MobAura extends Module {

    private final DoubleSetting range      = register(new DoubleSetting("Range",       "Attack range",             3.5, 1.0, 6.0));
    private final IntSetting    delay      = register(new IntSetting("Delay",          "Ticks between attacks",     8, 0, 20));
    private final DoubleSetting minHealth  = register(new DoubleSetting("Min Health",  "Only attack mobs above this HP", 0.0, 0.0, 20.0));

    private int ticker = 0;

    public MobAura() {
        super("MobAura", "Automatically attacks nearby hostile mobs — leaves players alone", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < delay.get()) return;
        ticker = 0;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof MobEntity mob)) continue;
            if (mob.isDead() || mob.getHealth() <= minHealth.get()) continue;
            if (mc.player.distanceTo(mob) > range.get()) continue;

            mc.interactionManager.attackEntity(mc.player, mob);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }
}
