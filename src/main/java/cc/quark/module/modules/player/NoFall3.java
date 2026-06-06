package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall3 extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Method used to cancel fall damage",
            "Packet", "Packet", "Flag", "Vanilla"));
    private final BoolSetting onlyAboveThreshold = register(new BoolSetting(
            "Threshold", "Only activate when fall distance > 3 blocks", true));

    public NoFall3() {
        super("NoFall3", "Cancels fall damage via ground packet spoofing", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (mode.get().equals("Flag")) {
            // Vanilla flag mode: force onGround = true client side
            if (mc.player.fallDistance > (onlyAboveThreshold.isEnabled() ? 3f : 0f)) {
                mc.player.fallDistance = 0f;
            }
        } else if (mode.get().equals("Vanilla")) {
            // Clear fall distance each tick
            mc.player.fallDistance = 0f;
        }
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!mode.get().equals("Packet")) return;
        if (mc.player == null) return;
        if (!(event.getPacket() instanceof PlayerMoveC2SPacket packet)) return;

        float fallDist = mc.player.fallDistance;
        if (onlyAboveThreshold.isEnabled() && fallDist < 3f) return;

        // Spoof onGround = true in movement packets so the server won't apply fall damage
        if (!packet.isOnGround()) {
            try {
                // Reflection to set onGround field
                java.lang.reflect.Field[] fields = PlayerMoveC2SPacket.class.getDeclaredFields();
                for (java.lang.reflect.Field f : fields) {
                    if (f.getType() == boolean.class && f.getName().contains("onGround")) {
                        f.setAccessible(true);
                        f.set(packet, true);
                        break;
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}
