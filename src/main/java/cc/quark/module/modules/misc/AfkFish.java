package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.Hand;

public class AfkFish extends Module {

    private final IntSetting castDelay = register(new IntSetting(
            "CastDelay", "Milliseconds after reeling before re-casting", 500, 100, 3000));

    private final BoolSetting requireRod = register(new BoolSetting(
            "RequireRod", "Only act if holding a fishing rod", true));

    private final BoolSetting announceEach = register(new BoolSetting(
            "Announce", "Print a message each time a fish is caught", false));

    private final TimerUtil castTimer = new TimerUtil();

    private boolean waitingToCast = false;
    private int catchCount = 0;

    public AfkFish() {
        super("AfkFish", "Automatically casts and reels fishing rod for AFK fishing", Category.MISC);
    }

    @Override
    public void onEnable() {
        castTimer.reset();
        waitingToCast = false;
        catchCount = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Ensure holding a fishing rod
        if (requireRod.isEnabled() && !(mc.player.getMainHandStack().getItem() instanceof FishingRodItem)) {
            // Try to switch to rod in hotbar
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == Items.FISHING_ROD) {
                    mc.player.getInventory().selectedSlot = i;
                    break;
                }
            }
            return;
        }

        // If the bobber is not in the world, cast
        if (mc.player.fishHook == null) {
            if (waitingToCast) {
                if (castTimer.hasReached(castDelay.get())) {
                    // Use the rod item to cast
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    waitingToCast = false;
                }
            } else {
                // Cast immediately
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                waitingToCast = false;
            }
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt)) return;

        FishingBobberEntity bobber = mc.player.fishHook;
        if (bobber == null) return;
        if (pkt.getId() != bobber.getId()) return;

        // Bobber got a velocity update = fish on the line — reel in and prepare to re-cast
        mc.execute(() -> {
            if (mc.player == null || mc.interactionManager == null) return;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND); // reel in
            catchCount++;
            if (announceEach.isEnabled()) {
                ChatUtil.info("AfkFish: Caught fish #" + catchCount + "!");
            }
            castTimer.reset();
            waitingToCast = true;
        });
    }

    @Override
    public String getSuffix() {
        return "x" + catchCount;
    }
}
