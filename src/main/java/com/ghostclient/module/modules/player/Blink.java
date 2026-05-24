package com.ghostclient.module.modules.player;

import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventPacketSend;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.EnumSetting;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Blink - buffers all outgoing movement packets and releases them at once on disable.
 *
 * Toggle mode: Enable to start buffering, disable to release all buffered packets.
 * Hold mode:   Buffer while keybind is held; release when released (use with keybind).
 */
public class Blink extends Module {

    public enum Mode {
        TOGGLE, HOLD
    }

    private final EnumSetting<Mode> mode = register(new EnumSetting<>(
            "Mode", "Toggle or Hold to buffer packets", Mode.TOGGLE));

    private final Deque<Packet<?>> bufferedPackets = new ArrayDeque<>();

    public Blink() {
        super("Blink", "Buffers movement packets and releases them on disable", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        bufferedPackets.clear();
    }

    @Override
    public void onDisable() {
        flushPackets();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerMoveC2SPacket)) return;

        // Buffer the movement packet instead of sending it
        bufferedPackets.add(event.getPacket());
        event.cancel();
    }

    /**
     * Sends all buffered packets in order.
     */
    private void flushPackets() {
        if (mc.player == null || mc.player.networkHandler == null) {
            bufferedPackets.clear();
            return;
        }

        for (Packet<?> packet : bufferedPackets) {
            mc.player.networkHandler.sendPacket(packet);
        }
        bufferedPackets.clear();
    }

    /**
     * Returns how many packets are currently buffered (for HUD display).
     */
    public int getBufferSize() {
        return bufferedPackets.size();
    }
}
