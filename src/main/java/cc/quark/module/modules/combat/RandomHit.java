package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * RandomHit - attacks a random in-range target each cycle.
 */
public class RandomHit extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 3.5, 1.0, 6.0));
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between hits", 10, 0, 20));

    private final Random random = new Random();
    private int ticks;

    public RandomHit() {
        super("RandomHit", "Attacks a random in-range target", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ticks++;
        if (ticks < delay.get()) return;
        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            targets.add(living);
        }
        if (!targets.isEmpty()) {
            LivingEntity target = targets.get(random.nextInt(targets.size()));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            ticks = 0;
        }
    }
}
