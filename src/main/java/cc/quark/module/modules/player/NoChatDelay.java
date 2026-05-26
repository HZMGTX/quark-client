package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.lang.reflect.Field;

public class NoChatDelay extends Module {

    private static Field lastChatTimestampField = null;

    static {
        for (Field f : ClientPlayNetworkHandler.class.getDeclaredFields()) {
            if (f.getType() == long.class
                    && (f.getName().contains("lastMessage")
                     || f.getName().contains("lastChat")
                     || f.getName().contains("timestamp"))) {
                f.setAccessible(true);
                lastChatTimestampField = f;
                break;
            }
        }
    }

    public NoChatDelay() {
        super("NoChatDelay", "Removes the client-side chat cooldown between messages", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!(event.getPacket() instanceof ChatMessageC2SPacket)) return;
        if (mc.getNetworkHandler() == null) return;

        resetChatCooldown();
    }

    private void resetChatCooldown() {
        ClientPlayNetworkHandler handler = mc.getNetworkHandler();
        if (handler == null) return;

        if (lastChatTimestampField != null) {
            try {
                lastChatTimestampField.set(handler, 0L);
                return;
            } catch (Exception ignored) {}
        }

        for (Field f : ClientPlayNetworkHandler.class.getDeclaredFields()) {
            if (f.getType() != long.class) continue;
            try {
                f.setAccessible(true);
                long val = (long) f.get(handler);
                long now = System.currentTimeMillis();
                if (val > now - 2000L && val < now + 1000L) {
                    f.set(handler, 0L);
                    lastChatTimestampField = f;
                }
            } catch (Exception ignored) {}
        }
    }
}
