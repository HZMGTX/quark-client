package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class PunchAura extends Module {

    private final DoubleSetting range          = register(new DoubleSetting("Range",          "Attack range",              3.5, 1.0, 6.0));
    private final IntSetting    knockbackLevel = register(new IntSetting("KnockbackLevel",    "Knockback power level",     2,   1,   5));

    private long lastAttackMs = 0L;

    public PunchAura() {
        super("PunchAura", "Knocks back enemies with punching", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastAttackMs = 0L;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float cooldown = mc.player.getAttackCooldownProgress(0f);
        if (cooldown < 0.9f) return;
        if (System.currentTimeMillis() - lastAttackMs < 200L) return;

        LivingEntity target = null;
        double bestDist = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(e);
            if (d < bestDist) {
                bestDist = d;
                target   = (LivingEntity) e;
            }
        }

        if (target == null) return;

        // Apply extra knockback manually after attack
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        // Apply additional velocity to push the target away
        Vec3d dir = target.getPos().subtract(mc.player.getPos()).normalize();
        double kbMult = knockbackLevel.get() * 0.4;
        Vec3d current = target.getVelocity();
        target.setVelocity(
            current.x + dir.x * kbMult,
            current.y + 0.3,
            current.z + dir.z * kbMult
        );

        lastAttackMs = System.currentTimeMillis();
    }
}
