package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

/**
 * BackstabAura - only attacks targets that are facing away from the player.
 */
public class BackstabAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Attack range", 3.5, 1.0, 6.0));
    private final DoubleSetting angle = register(new DoubleSetting("Angle", "Max facing-away angle", 60.0, 10.0, 90.0));

    public BackstabAura() {
        super("BackstabAura", "Attacks targets facing away from you", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player || !(entity instanceof LivingEntity living) || living.isDead()) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            float diff = MathHelper.wrapDegrees(entity.getYaw() - mc.player.getYaw());
            if (Math.abs(diff) > angle.get()) continue;
            mc.interactionManager.attackEntity(mc.player, living);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }
}
