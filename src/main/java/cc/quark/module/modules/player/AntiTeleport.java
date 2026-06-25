package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

/**
 * AntiTeleport — cancels incoming server-side teleport packets to prevent
 * the server from repositioning the player (useful for bypass scenarios).
 *
 * A cooldown prevents blocking legitimate spawn/respawn teleports for too long.
 */
public class AntiTeleport extends Module {

    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Send a warning message when a teleport is cancelled", true));

    private final IntSetting cooldownSec = register(new IntSetting(
            "Cooldown", "Seconds before allowing teleports again after enable", 5, 1, 30));

    private final IntSetting maxCancels = register(new IntSetting(
            "Max Cancels", "Maximum number of teleports to cancel before giving up", 10, 1, 100));

    private final BoolSetting respectRespawn = register(new BoolSetting(
            "Respect Respawn", "Allow teleports during respawn (position near 0,0)", true));

    private final TimerUtil enableTimer = new TimerUtil();
    private int cancelCount = 0;

    public AntiTeleport() {
        super("AntiTeleport", "Cancels incoming teleport packets from the server", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        cancelCount = 0;
        enableTimer.reset();
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!(event.getPacket() instanceof PlayerPositionLookS2CPacket)) return;
        if (mc.player == null) return;

        // After cooldown period, allow through to avoid permanent desync
        if (enableTimer.hasReached(cooldownSec.get() * 1000L)) return;

        // If we've cancelled too many times, stop
        if (cancelCount >= maxCancels.get()) return;

        // Optionally allow teleports when the player is near 0,0 (spawn area)
        if (respectRespawn.isEnabled()) {
            double px = mc.player.getX();
            double pz = mc.player.getZ();
            // Allow if the player is already near origin (likely a respawn teleport)
            if (Math.abs(px) < 16 && Math.abs(pz) < 16) return;
        }

        event.cancel();
        cancelCount++;

        if (notify.isEnabled()) {
            ChatUtil.warn("AntiTeleport: cancelled server teleport #" + cancelCount);
        }
    }
}
