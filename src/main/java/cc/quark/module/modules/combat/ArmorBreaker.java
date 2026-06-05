package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPreMotion;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class ArmorBreaker extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range to target armor weak points", 3.5, 1.0, 6.0));

    private LivingEntity target = null;

    public ArmorBreaker() {
        super("ArmorBreaker", "Prioritizes hitting armor weak points", Category.COMBAT);
    }

    @EventHandler
    public void onPreMotion(EventPreMotion event) {
        if (mc.player == null || mc.world == null) return;
        if (target == null) return;

        // Aim at feet for weak point hits
        Vec3d feet = target.getPos();
        double dx = feet.x - mc.player.getX();
        double dy = feet.y - mc.player.getY() + 0.1 - mc.player.getEyeHeight(mc.player.getPose());
        double dz = feet.z - mc.player.getZ();
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        event.setYaw(yaw);
        event.setPitch(pitch);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        target = null;
        double r = range.get();
        int lowestArmor = Integer.MAX_VALUE;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity p)) continue;
            if (p == mc.player) continue;
            if (p.isRemoved()) continue;
            if (mc.player.distanceTo(p) > r) continue;
            if (p.getArmor() < lowestArmor) {
                lowestArmor = p.getArmor();
                target = p;
            }
        }

        if (target != null && mc.player.getAttackCooldownProgress(0f) >= 1f) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
