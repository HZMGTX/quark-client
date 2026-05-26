package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoEgg - throws eggs at nearby targets automatically.
 */
public class AutoEgg extends Module {

    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between throws", 6, 1, 40));
    private final IntSetting range = register(new IntSetting("Range", "Detection range", 12, 1, 40));

    private int ticks;

    public AutoEgg() {
        super("AutoEgg", "Throws eggs at nearby targets", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.player.getMainHandStack().isOf(Items.EGG)) return;
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
