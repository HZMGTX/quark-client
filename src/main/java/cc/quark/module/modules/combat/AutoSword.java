package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

/**
 * AutoSword - automatically switches to a sword (or axe as fallback) when an
 * enemy player comes within range, and restores the previous slot when they leave.
 */
public class AutoSword extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Distance to trigger sword switch (blocks)", 5.0, 2.0, 10.0));

    private final BoolSetting useAxe = register(new BoolSetting(
            "Use Axe", "Fall back to axe if no sword is found", true));

    private final BoolSetting restoreSlot = register(new BoolSetting(
            "Restore Slot", "Switch back to original slot when out of range", true));

    private int previousSlot = -1;
    private boolean switched = false;

    public AutoSword() {
        super("AutoSword", "Auto-switches to sword when enemy is in range", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (switched && restoreSlot.isEnabled() && previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = previousSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        }
        switched = false;
        previousSlot = -1;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean enemyNearby = isEnemyInRange();

        if (enemyNearby && !switched) {
            int swordSlot = findWeaponSlot();
            if (swordSlot == -1) return;
            previousSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = swordSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(swordSlot));
            switched = true;
        } else if (!enemyNearby && switched) {
            if (restoreSlot.isEnabled() && previousSlot != -1) {
                mc.player.getInventory().selectedSlot = previousSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            }
            switched = false;
            previousSlot = -1;
        }
    }

    private boolean isEnemyInRange() {
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            if (mc.player.distanceTo(p) <= range.get()) return true;
        }
        return false;
    }

    private int findWeaponSlot() {
        // Search hotbar (slots 0-8)
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof SwordItem) {
                return i;
            }
        }
        if (useAxe.isEnabled()) {
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() instanceof AxeItem) {
                    return i;
                }
            }
        }
        return -1;
    }
}
