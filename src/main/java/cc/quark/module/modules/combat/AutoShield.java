package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

/**
 * AutoShield - raises a held shield when an enemy is close.
 */
public class AutoShield extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Activation range", 4.0, 1.0, 8.0));

    public AutoShield() {
        super("AutoShield", "Raises a held shield when an enemy is close", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        boolean hasShield = mc.player.getMainHandStack().getItem() instanceof ShieldItem
                || mc.player.getOffHandStack().getItem() instanceof ShieldItem;
        if (!hasShield) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) <= range.get()) {
                if (!mc.player.isUsingItem()) {
                    mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                }
                return;
            }
        }
    }
}
