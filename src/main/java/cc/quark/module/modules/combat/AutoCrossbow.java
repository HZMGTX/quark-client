package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoCrossbow extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to auto-fire crossbow", 15.0, 5.0, 25.0));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between shots", 600, 100, 3000));

    private long lastShot = 0;
    private boolean isCharging = false;
    private int chargeTicks = 0;

    public AutoCrossbow() {
        super("AutoCrossbow", "Auto-loads and fires crossbow", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        isCharging = false;
        chargeTicks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ItemStack held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof CrossbowItem crossbow)) return;

        boolean loaded = CrossbowItem.isCharged(held);

        if (!loaded) {
            // Charge the crossbow
            if (!isCharging) {
                isCharging = true;
                chargeTicks = 0;
                mc.options.useKey.setPressed(true);
            } else {
                chargeTicks++;
                if (chargeTicks >= 25) {
                    mc.options.useKey.setPressed(false);
                    isCharging = false;
                    chargeTicks = 0;
                }
            }
            return;
        }

        if (System.currentTimeMillis() - lastShot < delay.get()) return;

        // Find target and fire
        double r = range.get();
        LivingEntity target = null;
        double closestDist = Double.MAX_VALUE;
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (living == mc.player || living.isDead()) continue;
            double dist = mc.player.distanceTo(living);
            if (dist <= r && dist < closestDist) {
                closestDist = dist;
                target = living;
            }
        }

        if (target != null) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            lastShot = System.currentTimeMillis();
        }
    }
}
