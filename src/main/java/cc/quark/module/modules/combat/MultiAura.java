package cc.quark.module.modules.combat;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

/**
 * MultiAura - attacks multiple targets in range each cycle.
 */
public class MultiAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 3.5, 1.0, 6.0));
    private final IntSetting maxTargets = register(new IntSetting("Max Targets", "Targets per cycle", 3, 1, 10));
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between cycles", 5, 0, 20));

    private int ticks;

    public MultiAura() {
        super("MultiAura", "Attacks multiple targets at once", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ticks++;
        if (ticks < delay.get()) return;

        int hit = 0;
        for (Entity entity : mc.world.getEntities()) {
            if (hit >= maxTargets.get()) break;
            if (entity == mc.player || !(entity instanceof LivingEntity living)) continue;
            if (living.isDead()) continue;
            if (entity instanceof PlayerEntity player
                    && Quark.getInstance().getFriendManager().isFriend(player.getGameProfile().getName())) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            hit++;
        }
        if (hit > 0) ticks = 0;
    }
}
