package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;

import java.util.Comparator;
import java.util.Optional;

public class AutoLevelUp extends Module {

    private final IntSetting targetLevel = register(new IntSetting(
            "TargetLevel", "XP level to grind toward before stopping", 30, 1, 100));

    private final TimerUtil timer = new TimerUtil();

    public AutoLevelUp() {
        super("AutoLevelUp", "Grinds XP by repeatedly attacking nearby mobs", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.experienceLevel >= targetLevel.get()) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        Optional<MobEntity> nearest = mc.world.getEntitiesByClass(MobEntity.class,
                mc.player.getBoundingBox().expand(4.0), e -> e.isAlive() && !e.isInvulnerable())
                .stream()
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));

        nearest.ifPresent(mob -> mc.interactionManager.attackEntity(mc.player, mob));
    }
}
