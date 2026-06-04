package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class SwordFisher extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to hook players", 5.0, 1.0, 8.0));

    private final TimerUtil reelTimer = new TimerUtil();
    private boolean rodThrown = false;

    public SwordFisher() {
        super("SwordFisher", "Fishes players by hooking and reeling while attacking", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        rodThrown = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Check if player has a fishing rod
        int rodSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.FISHING_ROD) {
                rodSlot = i;
                break;
            }
        }
        if (rodSlot < 0) return;

        // Find enemy in range
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

        if (target == null) {
            rodThrown = false;
            return;
        }

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = rodSlot;

        if (!rodThrown) {
            // Cast rod
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            rodThrown = true;
            reelTimer.reset();
        } else if (reelTimer.hasReached(400)) {
            // Reel in to pull target and deal damage
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            rodThrown = false;
            reelTimer.reset();

            // Also swing sword
            if (mc.player.getAttackCooldownProgress(0.0f) >= 0.9f) {
                mc.player.getInventory().selectedSlot = prevSlot;
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }

        mc.player.getInventory().selectedSlot = prevSlot;
    }
}
