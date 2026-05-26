package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * RodAura - reels a fishing rod at nearby targets repeatedly.
 */
public class RodAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Detection range", 6.0, 1.0, 12.0));
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between casts", 8, 1, 40));

    private int ticks;

    public RodAura() {
        super("RodAura", "Reels a fishing rod at nearby targets", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.player.getMainHandStack().isOf(Items.FISHING_ROD)) return;
        ticks++;
        if (ticks < delay.get()) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            ticks = 0;
            return;
        }
    }
}
