package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * AntiInvisible - attacks nearby players even when they are invisible.
 *
 * Invisible players still have a valid entity position and bounding box;
 * they just render with alpha 0. This module scans for PlayerEntity instances
 * within range, checks the invisible flag, and attacks them normally.
 */
public class AntiInvisible extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range for invisible players", 4.0, 1.0, 6.0));

    private final BoolSetting onlyInvisible = register(new BoolSetting(
            "Only Invisible", "Only target invisible players; ignore visible ones", true));

    private final BoolSetting requireCooldown = register(new BoolSetting(
            "Require Cooldown", "Wait for full attack cooldown before striking", true));

    private final BoolSetting swing = register(new BoolSetting(
            "Swing", "Play hand-swing animation on attack", true));

    public AntiInvisible() {
        super("AntiInvisible", "Attacks invisible players nearby", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (requireCooldown.isEnabled() && mc.player.getAttackCooldownProgress(0f) < 1.0f) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity target)) continue;
            if (target.isRemoved() || target.getHealth() <= 0f) continue;

            if (onlyInvisible.isEnabled() && !target.isInvisible()) continue;

            double dist = mc.player.distanceTo(target);
            if (dist > range.get()) continue;

            mc.interactionManager.attackEntity(mc.player, target);
            if (swing.isEnabled()) mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND);
            break; // one attack per tick
        }
    }
}
