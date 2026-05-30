package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/**
 * AntiFireball2 — reflects fireballs and small fireballs by attacking them,
 * and optionally dodges by moving perpendicular when they are close.
 */
public class AntiFireball2 extends Module {

    private final IntSetting  range        = register(new IntSetting ("Range",        "Detection range in blocks",             5,    1, 15));
    private final BoolSetting autoReflect  = register(new BoolSetting("AutoReflect",  "Attack fireballs to reflect them",      true));
    private final BoolSetting dodge        = register(new BoolSetting("Dodge",        "Strafe sideways when fireball is close", true));

    public AntiFireball2() {
        super("AntiFireball2", "Reflects fireballs and small fireballs, with optional dodge", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        double r = range.get();

        for (Entity entity : mc.world.getEntities()) {
            boolean isLarge = entity instanceof FireballEntity;
            boolean isSmall = entity instanceof SmallFireballEntity;
            if (!isLarge && !isSmall) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > r) continue;

            if (autoReflect.isEnabled()) {
                mc.interactionManager.attackEntity(mc.player, entity);
                mc.player.swingHand(Hand.MAIN_HAND);
            }

            if (dodge.isEnabled()) {
                Vec3d dir = mc.player.getPos().subtract(entity.getPos()).normalize();
                // Strafe perpendicular (rotate 90 degrees around Y)
                Vec3d strafe = new Vec3d(-dir.z, 0, dir.x).multiply(0.3);
                mc.player.addVelocity(strafe.x, 0, strafe.z);
            }

            return; // handle one fireball per tick
        }
    }
}
