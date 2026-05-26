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

import java.util.Random;

/**
 * AuraDelay - attacks the closest target with a randomized inter-hit delay.
 */
public class AuraDelay extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 3.5, 1.0, 6.0));
    private final IntSetting minDelay = register(new IntSetting("Min Delay", "Minimum ticks between hits", 6, 0, 20));
    private final IntSetting maxDelay = register(new IntSetting("Max Delay", "Maximum ticks between hits", 12, 0, 40));

    private final Random random = new Random();
    private int ticks;
    private int target;

    public AuraDelay() {
        super("AuraDelay", "Attacks with a randomized delay", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticks = 0;
        target = minDelay.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ticks++;
        if (ticks < target) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            ticks = 0;
            int lo = Math.min(minDelay.get(), maxDelay.get());
            int hi = Math.max(minDelay.get(), maxDelay.get());
            target = lo + (hi > lo ? random.nextInt(hi - lo + 1) : 0);
            return;
        }
    }
}
