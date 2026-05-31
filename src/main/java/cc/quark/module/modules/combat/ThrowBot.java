package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

/**
 * ThrowBot - automatically throws combat projectiles (potions, snowballs) at
 * the nearest enemy player.
 *
 * <p>Priority order: splash potions → snowballs (configurable).
 */
public class ThrowBot extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum throw range in blocks", 10.0, 3.0, 20.0));

    private final IntSetting throwCooldownMs = register(new IntSetting(
            "Cooldown", "Milliseconds between throws", 1500, 200, 10000));

    private final BoolSetting throwPotions = register(new BoolSetting(
            "Potions", "Throw splash potions at enemies", true));

    private final BoolSetting throwSnowballs = register(new BoolSetting(
            "Snowballs", "Throw snowballs when no potions available", true));

    private final BoolSetting restoreSlot = register(new BoolSetting(
            "Restore Slot", "Return to original slot after throwing", true));

    private final TimerUtil throwTimer = new TimerUtil();
    private int previousSlot = -1;

    public ThrowBot() {
        super("ThrowBot", "Auto-throws potions/snowballs at nearby enemies", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        throwTimer.reset();
        previousSlot = -1;
    }

    @Override
    public void onDisable() {
        if (restoreSlot.isEnabled() && previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = previousSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!throwTimer.hasReached(throwCooldownMs.get())) return;

        PlayerEntity target = findNearestTarget();
        if (target == null) return;

        int throwSlot = findThrowableSlot();
        if (throwSlot == -1) return;

        previousSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = throwSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(throwSlot));

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        if (restoreSlot.isEnabled()) {
            mc.player.getInventory().selectedSlot = previousSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            previousSlot = -1;
        }

        throwTimer.reset();
    }

    private PlayerEntity findNearestTarget() {
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(p);
            if (d <= range.get() && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private int findThrowableSlot() {
        // Priority: splash potions first
        if (throwPotions.isEnabled()) {
            for (int i = 0; i < 9; i++) {
                Item item = mc.player.getInventory().getStack(i).getItem();
                if (item instanceof SplashPotionItem) return i;
            }
        }
        // Fallback: snowballs
        if (throwSnowballs.isEnabled()) {
            for (int i = 0; i < 9; i++) {
                Item item = mc.player.getInventory().getStack(i).getItem();
                if (item instanceof SnowballItem || item == Items.SNOWBALL) return i;
            }
        }
        return -1;
    }
}
