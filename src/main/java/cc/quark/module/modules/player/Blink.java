package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventRender3D;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Box;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Blink - buffers all outgoing movement packets and releases them at once on disable.
 *
 * Toggle mode: Enable to start buffering, disable to release all buffered packets.
 * Shows a ghost indicator at the "server-side" position while blink is active.
 */
public class Blink extends Module {

    private final IntSetting maxPackets = register(new IntSetting(
            "Max Packets", "Maximum stored packets before auto-flush", 50, 5, 200));

    private final BoolSetting visual = register(new BoolSetting(
            "Visual", "Show ghost player at the server-side position while blink is active", true));

    private final Deque<Packet<?>> bufferedPackets = new ArrayDeque<>();

    // The position where the player was when Blink was enabled (server-side position)
    private double ghostX, ghostY, ghostZ;
    private boolean hasGhostPos = false;

    public Blink() {
        super("Blink", "Buffers movement packets and releases them on disable", Category.PLAYER);
    }

    @Override
    public String getSuffix() {
        return String.valueOf(bufferedPackets.size());
    }

    @Override
    public void onEnable() {
        bufferedPackets.clear();
        if (mc.player != null) {
            ghostX = mc.player.getX();
            ghostY = mc.player.getY();
            ghostZ = mc.player.getZ();
            hasGhostPos = true;
        } else {
            hasGhostPos = false;
        }
    }

    @Override
    public void onDisable() {
        flushPackets();
        hasGhostPos = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        // Auto-flush if max packets reached
        if (bufferedPackets.size() >= maxPackets.get()) {
            flushPackets();
            // Re-record ghost position
            if (mc.player != null) {
                ghostX = mc.player.getX();
                ghostY = mc.player.getY();
                ghostZ = mc.player.getZ();
            }
        }
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof PlayerMoveC2SPacket)) return;

        // Buffer the movement packet instead of sending it
        bufferedPackets.add(event.getPacket());
        event.cancel();
    }

    @EventHandler
    public void onRender3D(EventRender3D event) {
        if (!visual.isEnabled() || !hasGhostPos || mc.player == null) return;

        // Draw a wireframe box at the ghost (server-side) position
        Box box = new Box(
                ghostX - 0.3, ghostY, ghostZ - 0.3,
                ghostX + 0.3, ghostY + 1.8, ghostZ + 0.3
        );
        RenderUtil.drawESPBox(event.getMatrixStack(), box, 0.3f, 0.6f, 1.0f, 0.8f, 1.5f);
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
