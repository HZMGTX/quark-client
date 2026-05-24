package com.ghostclient.module.modules.player;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventPacketSend;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

/**
 * NoHunger - prevents the client from sending packets that cause hunger loss.
 *
 * Hunger drains due to movement (sprint packets) and general food consumption ticks.
 * By cancelling sprint-related packets we stop the server from reducing hunger.
 * The FoodStatsPacket is server->client, so we intercept sprint on client->server side.
 *
 * Note: On vanilla servers this will still lose hunger over time server-side;
 * this module works best as a complement to other movement exploits.
 */
public class NoHunger extends Module {

    public NoHunger() {
        super("NoHunger", "Prevents hunger loss by suppressing sprint packets", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;

        // Prevent slot-change packets that could reset food tracking
        // The primary hunger prevention is done via the mixin in MixinClientPlayerEntity
        // which intercepts the sprint flag. Here we supplement by ensuring
        // the player does not send UpdateSelectedSlot while food is active uselessly.
        if (event.getPacket() instanceof UpdateSelectedSlotC2SPacket pkt) {
            // Allow all slot changes - this packet itself doesn't cause hunger
            // We leave it uncancelled but can hook here for future logic
        }
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            // Force the client food level to max so vanilla code won't try to drain it
            mc.player.getHungerManager().setFoodLevel(20);
            mc.player.getHungerManager().setSaturationLevel(5.0f);
        }
    }

    @Override
    public void onDisable() {
        // Nothing to undo - server state is authoritative
    }
}
