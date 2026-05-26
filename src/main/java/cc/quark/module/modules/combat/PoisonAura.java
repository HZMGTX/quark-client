package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.util.Hand;

/**
 * PoisonAura - throws a held splash potion at nearby targets.
 */
public class PoisonAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Trigger range", 5.0, 1.0, 10.0));

    public PoisonAura() {
        super("PoisonAura", "Throws splash potions at nearby targets", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof SplashPotionItem)) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) <= range.get()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }
    }
}
