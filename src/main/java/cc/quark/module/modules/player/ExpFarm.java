package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class ExpFarm extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to auto-attack mobs for XP (blocks)", 4.0, 1.0, 8.0));

    public ExpFarm() {
        super("ExpFarm", "Auto-kills mobs to farm XP", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        double r = range.get();
        double rSq = r * r;

        List<MobEntity> mobs = mc.world.getEntitiesByClass(
                MobEntity.class,
                mc.player.getBoundingBox().expand(r),
                mob -> !mob.isRemoved()
                        && mob.squaredDistanceTo(mc.player) <= rSq
                        && mob.isAlive()
        );

        if (mobs.isEmpty()) return;

        // Attack the nearest mob
        MobEntity target = mobs.stream()
                .min(Comparator.comparingDouble(m -> m.squaredDistanceTo(mc.player)))
                .orElse(null);

        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
