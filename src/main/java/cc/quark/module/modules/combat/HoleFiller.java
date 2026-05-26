package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * HoleFiller - places blocks toward an enemy standing in a hole nearby.
 */
public class HoleFiller extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Trigger range", 4.0, 1.0, 8.0));

    public HoleFiller() {
        super("HoleFiller", "Places blocks on enemies in holes", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        boolean hasBlock = mc.player.getMainHandStack().isOf(Items.OBSIDIAN)
                || mc.player.getMainHandStack().isOf(Items.COBBLESTONE);
        if (!hasBlock) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof PlayerEntity player) || player.isDead()) continue;
            if (mc.player.distanceTo(entity) <= range.get()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }
    }
}
