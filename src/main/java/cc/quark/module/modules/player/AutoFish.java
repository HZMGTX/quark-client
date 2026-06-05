package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.Hand;

/**
 * AutoFish - automatically casts the fishing rod and reels in when something is caught.
 *
 * Detection: watches for the bobber entity's Y velocity dropping (it dips when a fish bites),
 * or listens for EntityVelocityUpdateS2CPacket/PlaySoundS2CPacket indicating a catch.
 */
public class AutoFish extends Module {

    private final BoolSetting autoCast = register(new BoolSetting(
            "Auto Cast", "Automatically re-cast rod after catching fish", true));

    private final BoolSetting chatAnnounce = register(new BoolSetting(
            "Chat Announce", "Send a chat message when fish is caught", false));

    private final IntSetting castDelaySetting = register(new IntSetting(
            "Cast Delay", "Ticks to wait before re-casting after catching", 15, 10, 100));

    private int castDelay = 0;
    private boolean waitingForCatch = false;
    private FishingBobberEntity lastBobber = null;
    private double lastBobberY = Double.MAX_VALUE;
    private boolean justCaught = false;

    public AutoFish() {
        super("AutoFish", "Auto-casts rod and reels in when bobber dips", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        castDelay = 0;
        waitingForCatch = false;
        lastBobber = null;
        lastBobberY = Double.MAX_VALUE;
        justCaught = false;
    }

    private Hand getRodHand() {
        if (mc.player == null) return null;
        if (mc.player.getMainHandStack().getItem() == Items.FISHING_ROD) return Hand.MAIN_HAND;
        if (mc.player.getOffHandStack().getItem() == Items.FISHING_ROD) return Hand.OFF_HAND;
        return null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        Hand rodHand = getRodHand();
        if (rodHand == null) return;

        // Cast delay after catching
        if (castDelay > 0) {
            castDelay--;
            if (castDelay == 0 && autoCast.isEnabled()) {
                // Re-cast the rod
                mc.interactionManager.interactItem(mc.player, rodHand);
                lastBobberY = Double.MAX_VALUE;
            }
            return;
        }

        FishingBobberEntity bobber = mc.player.fishHook;

        if (bobber == null) {
            // Not fishing — cast the rod if autoCast or first time
            if (autoCast.isEnabled() && !justCaught) {
                mc.interactionManager.interactItem(mc.player, rodHand);
            }
            justCaught = false;
            waitingForCatch = false;
            lastBobberY = Double.MAX_VALUE;
            lastBobber = null;
            return;
        }

        lastBobber = bobber;

        // Detect a bite: bobber dips (Y goes from positive velocity to negative, bobber moves down)
        double currentY = bobber.getY();
        if (lastBobberY != Double.MAX_VALUE) {
            double deltaY = currentY - lastBobberY;
            // A negative dip of more than 0.08 blocks indicates a fish biting
            if (deltaY < -0.08) {
                reelIn(rodHand);
                return;
            }
        }
        lastBobberY = currentY;
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null || mc.interactionManager == null) return;

        Hand rodHand = getRodHand();
        if (rodHand == null) return;

        if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket pkt) {
            FishingBobberEntity bobber = mc.player.fishHook;
            if (bobber == null) return;

            if (pkt.getEntityId() != bobber.getId()) return;

            // Bobber velocity spike (downward) means fish caught
            double vy = pkt.getVelocityY() / 8000.0;
            if (vy < -0.1) {
                reelIn(rodHand);
            }
        } else if (event.getPacket() instanceof PlaySoundS2CPacket soundPkt) {
            // Listen for fishing splash sound (indicates catch)
            String soundId = soundPkt.getSound().value().getId().getPath();
            if (soundId.contains("fishing") && soundId.contains("splash")) {
                FishingBobberEntity bobber = mc.player.fishHook;
                if (bobber != null) {
                    reelIn(rodHand);
                }
            }
        }
    }

    private void reelIn(Hand rodHand) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (castDelay > 0) return; // already reeling

        mc.interactionManager.interactItem(mc.player, rodHand);
        justCaught = true;
        castDelay = castDelaySetting.get();
        lastBobberY = Double.MAX_VALUE;
        lastBobber = null;

        if (chatAnnounce.isEnabled() && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatMessage("Fish caught!");
        }
    }
}
