package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * FireAura — throws a fire charge at the nearest enemy by finding it in the
 * hotbar, switching to it, using it (throws as projectile), then restoring
 * the previous slot.  Falls back to flint-and-steel interaction if no fire
 * charge is found.
 */
public class FireAura extends Module {

    private final DoubleSetting range   = register(new DoubleSetting("Range",   "Target range in blocks",     5.0, 1.0, 10.0));
    private final IntSetting    delayMs = register(new IntSetting   ("Delay",   "Ms between throws",          600, 100, 3000));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public FireAura() {
        super("FireAura", "Throws fire charges at the nearest enemy", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        // Find nearest enemy
        LivingEntity target = null;
        double best = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity living)) continue;
            if (living.isDead() || living.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(e);
            if (d < best) { best = d; target = living; }
        }
        if (target == null) { restoreSlot(); return; }

        // Aim at target
        float savedYaw   = mc.player.getYaw();
        float savedPitch = mc.player.getPitch();
        double dx = target.getX() - mc.player.getX();
        double dy = target.getEyeY() - mc.player.getEyeY();
        double dz = target.getZ() - mc.player.getZ();
        mc.player.setYaw((float) Math.toDegrees(Math.atan2(-dx, dz)));
        mc.player.setPitch((float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx*dx+dz*dz))));

        // Find fire charge or flint-and-steel in hotbar
        int itemSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.FIRE_CHARGE)) { itemSlot = i; break; }
        }
        if (itemSlot == -1) {
            for (int i = 0; i < 9; i++) {
                ItemStack s = mc.player.getInventory().getStack(i);
                if (s.isOf(Items.FLINT_AND_STEEL)) { itemSlot = i; break; }
            }
        }

        if (itemSlot == -1) {
            mc.player.setYaw(savedYaw); mc.player.setPitch(savedPitch);
            return;
        }

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != itemSlot) { prevSlot = cur; mc.player.getInventory().selectedSlot = itemSlot; }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.setYaw(savedYaw); mc.player.setPitch(savedPitch);
        timer.reset();
        restoreSlot();
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
