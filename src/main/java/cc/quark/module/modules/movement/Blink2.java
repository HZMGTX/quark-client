package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Blink2 - buffers outgoing movement packets on the client side.  The server
 * sees the player frozen at their last sent position.  When the key is pressed
 * (default: V) or the auto-flush timer fires, all buffered packets are sent in
 * one burst, making the player "teleport" to their current position.
 */
public class Blink2 extends Module {

    private final IntSetting flushKey = register(new IntSetting(
            "Flush Key", "GLFW key code to manually flush buffered packets", GLFW.GLFW_KEY_V, 0, 400));
    private final IntSetting maxBufferMs = register(new IntSetting(
            "Max Buffer Ms", "Auto-flush after this many milliseconds (0 = never)", 3000, 0, 10000));

    private final List<Packet<?>> buffer = new ArrayList<>();
    private final TimerUtil timer = new TimerUtil();

    public Blink2() {
        super("Blink2", "Buffer movement packets then flush them all at once", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        buffer.clear();
        timer.reset();
    }

    @Override
    public void onDisable() {
        flush();
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;
        if (event.getPacket() instanceof PlayerMoveC2SPacket) {
            buffer.add(event.getPacket());
            event.cancel();
        }
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (event.getKeyCode() == flushKey.get()) {
            flush();
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        int maxMs = maxBufferMs.get();
        if (maxMs > 0 && timer.hasReached(maxMs)) {
            flush();
            timer.reset();
        }
    }

    private void flush() {
        if (mc.player == null || mc.getNetworkHandler() == null) {
            buffer.clear();
            return;
        }
        for (Packet<?> pkt : buffer) {
            mc.getNetworkHandler().sendPacket(pkt);
        }
        buffer.clear();
        timer.reset();
    }
}
