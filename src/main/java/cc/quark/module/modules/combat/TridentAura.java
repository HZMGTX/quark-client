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
 * TridentAura - attacks nearby targets while holding a trident.
 */
public class TridentAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 4.0, 1.0, 6.0));
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between attacks", 10, 0, 20));

    private int ticks;

    public TridentAura() {
        super("TridentAura", "Attacks targets while holding a trident", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ticks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.player.getMainHandStack().isOf(Items.TRIDENT)) return;
        ticks++;
        if (ticks < delay.get()) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            ticks = 0;
            return;
        }
    }
}
