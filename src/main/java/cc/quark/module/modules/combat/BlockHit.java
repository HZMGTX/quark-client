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
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class BlockHit extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range while blocking", 3.5, 1.0, 6.0));

    private final BoolSetting onlyNearby = register(new BoolSetting(
            "Only Nearby", "Only hit when enemy is within melee range", true));

    public BlockHit() {
        super("BlockHit", "Hits while blocking to deal extra knockback", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Must be actively blocking (using item that blocks)
        if (!mc.player.isBlocking()) return;

        float cooldown = mc.player.getAttackCooldownProgress(0.0f);
        if (cooldown < 0.9f) return;

        LivingEntity target = null;
        double minDist = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            LivingEntity living = (LivingEntity) entity;
            if (living.isDead() || living.getHealth() <= 0f) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist > range.get()) continue;
            if (dist < minDist) {
                minDist = dist;
                target = living;
            }
        }

        if (target != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
