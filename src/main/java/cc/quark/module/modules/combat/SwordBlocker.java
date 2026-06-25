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

public class SwordBlocker extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to check for enemies", 3.0, 1.0, 6.0));

    private final BoolSetting onlyNearEnemy = register(new BoolSetting(
            "Only Near Enemy", "Only block when an enemy is nearby", true));

    public SwordBlocker() {
        super("SwordBlocker", "Auto-right-clicks to block with sword (1.8 style)", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        boolean shouldBlock = false;

        if (onlyNearEnemy.isEnabled()) {
            double r = range.get();
            for (Entity entity : mc.world.getEntities()) {
                if (entity == mc.player) continue;
                if (!(entity instanceof LivingEntity living)) continue;
                if (living.isRemoved() || living.getHealth() <= 0f) continue;
                if (!(entity instanceof PlayerEntity)) continue;
                double dist = mc.player.distanceTo(entity);
                if (dist <= r) {
                    shouldBlock = true;
                    break;
                }
            }
        } else {
            shouldBlock = true;
        }

        if (shouldBlock) {
            mc.options.useKey.setPressed(true);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        } else {
            mc.options.useKey.setPressed(false);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.useKey.setPressed(false);
        }
    }
}
