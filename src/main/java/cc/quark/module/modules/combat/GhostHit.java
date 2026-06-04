package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class GhostHit extends Module {

    private final DoubleSetting range       = register(new DoubleSetting("Range",       "Attack range through walls", 6.0, 2.0, 12.0));
    private final BoolSetting   onlyVisible = register(new BoolSetting("OnlyVisible",   "Require line-of-sight",      true));

    private long lastAttackMs = 0L;

    public GhostHit() {
        super("GhostHit", "Attacks entities through walls server-side", Category.COMBAT);
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
        if (System.currentTimeMillis() - lastAttackMs < 50L) return;

        LivingEntity target = null;
        double bestDist = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity)) continue;
            double d = mc.player.distanceTo(e);
            if (d > bestDist) continue;

            if (onlyVisible.isEnabled() && !mc.player.canSee(e)) continue;

            bestDist = d;
            target   = (LivingEntity) e;
        }

        if (target == null) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttackMs = System.currentTimeMillis();
    }
}
