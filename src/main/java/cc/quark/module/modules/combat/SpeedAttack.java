package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class SpeedAttack extends Module {

    private final DoubleSetting minCooldown = register(new DoubleSetting(
            "MinCooldown", "Attack when cooldown reaches this fraction (0-1)", 0.5, 0.1, 1.0));

    public SpeedAttack() {
        super("SpeedAttack", "Attack at maximum allowed speed", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float cooldown = mc.player.getAttackCooldownProgress(0f);
        if (cooldown < minCooldown.get()) return;

        // Find nearest player target
        LivingEntity target = null;
        double bestDist     = 3.5;
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

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    @Override
    public String getSuffix() {
        if (mc.player == null) return "";
        return String.format("%.0f%%", mc.player.getAttackCooldownProgress(0f) * 100);
    }
}
