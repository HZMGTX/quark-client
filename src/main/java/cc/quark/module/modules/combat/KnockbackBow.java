package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.util.Hand;

/**
 * KnockbackBow - draws and releases a bow when a target is close for knockback.
 */
public class KnockbackBow extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Trigger range", 4.0, 1.0, 8.0));

    public KnockbackBow() {
        super("KnockbackBow", "Releases a bow shot for knockback when close", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BowItem)) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) <= range.get()) {
                if (mc.player.isUsingItem()) {
                    mc.player.stopUsingItem();
                } else {
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                }
                return;
            }
        }
    }
}
