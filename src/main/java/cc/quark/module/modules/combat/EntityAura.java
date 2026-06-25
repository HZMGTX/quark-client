package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class EntityAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range for mobs/entities", 4.0, 1.0, 8.0));
    private final IntSetting speed = register(new IntSetting("Speed", "Milliseconds between attacks", 300, 50, 2000));
    private final BoolSetting neutral = register(new BoolSetting("Neutral", "Attack neutral mobs (spiders, endermen etc.)", true));
    private final BoolSetting passive = register(new BoolSetting("Passive", "Attack passive animals (cows, pigs etc.)", false));

    private final TimerUtil timer = new TimerUtil();

    public EntityAura() {
        super("EntityAura", "Attacks any entities (mobs) nearby", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(speed.get())) return;

        float cooldown = mc.player.getAttackCooldownProgress(0f);
        if (cooldown < 0.9f) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> e == mc.player);
        targets.removeIf(e -> {
            if (e instanceof AnimalEntity) return !passive.isEnabled();
            if (e instanceof MobEntity) return !neutral.isEnabled();
            return true; // filter out players here (EntityAura is mob-only)
        });

        if (targets.isEmpty()) return;

        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        LivingEntity target = targets.get(0);

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }
}
