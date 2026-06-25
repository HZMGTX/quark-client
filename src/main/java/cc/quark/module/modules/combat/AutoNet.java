package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

/**
 * AutoNet - automatically throws a fishing rod at nearby enemies to apply the
 * hooked slow-down effect, giving the user a combat advantage.
 *
 * <p>When an enemy is within range the module:
 * <ol>
 *   <li>Switches to the fishing rod slot.</li>
 *   <li>Uses the rod to hook the target (right-click).</li>
 *   <li>Waits for a configurable delay, then retracts to deal a small pull.</li>
 *   <li>Restores the previous hotbar slot.</li>
 * </ol>
 */
public class AutoNet extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Maximum distance to target enemies (blocks)", 5.0, 2.0, 10.0));

    private final IntSetting retractDelay = register(new IntSetting(
            "Retract Delay", "Ticks to wait before retracting the rod", 10, 2, 60));

    private final IntSetting cooldownMs = register(new IntSetting(
            "Cooldown", "Milliseconds between casts", 2000, 500, 10000));

    private final BoolSetting restoreSlot = register(new BoolSetting(
            "Restore Slot", "Return to previous slot after casting", true));

    private final TimerUtil castTimer = new TimerUtil();
    private int previousSlot = -1;
    private int retractTicksLeft = 0;
    private boolean casting = false;

    public AutoNet() {
        super("AutoNet", "Throws fishing rod to slow and hook enemies", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        retractTicksLeft = 0;
        casting = false;
        previousSlot = -1;
    }

    @Override
    public void onDisable() {
        if (restoreSlot.isEnabled() && previousSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = previousSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
        }
        retractTicksLeft = 0;
        casting = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Handle retract phase
        if (casting) {
            retractTicksLeft--;
            if (retractTicksLeft <= 0) {
                // Retract rod (second right-click retracts)
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                casting = false;
                if (restoreSlot.isEnabled() && previousSlot != -1) {
                    mc.player.getInventory().selectedSlot = previousSlot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
                    previousSlot = -1;
                }
                castTimer.reset();
            }
            return;
        }

        if (!castTimer.hasReached(cooldownMs.get())) return;

        LivingEntity target = findNearestTarget();
        if (target == null) return;

        int rodSlot = findFishingRodSlot();
        if (rodSlot == -1) return;

        previousSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = rodSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(rodSlot));

        // Cast the rod
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        casting = true;
        retractTicksLeft = retractDelay.get();
    }

    private LivingEntity findNearestTarget() {
        LivingEntity best = null;
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

    private int findFishingRodSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof FishingRodItem) {
                return i;
            }
        }
        return -1;
    }
}
