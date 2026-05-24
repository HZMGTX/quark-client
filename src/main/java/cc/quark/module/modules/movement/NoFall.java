package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;

/**
 * NoFall - prevents fall damage using different server-side techniques.
 *
 * <p>Modes:
 * <ul>
 *   <li><b>Packet</b>      - sends onGround=true before impact when fallDistance > 2 and
 *       Y velocity is sufficiently negative.</li>
 *   <li><b>Predict</b>     - predicts when the player will hit the ground and sends the
 *       onGround packet exactly 1 tick before impact.</li>
 *   <li><b>SpoofGround</b> - always sends onGround=true while the player is falling,
 *       continuously resetting the server-side fall distance.</li>
 *   <li><b>NoGround</b>    - zeroes the client-side fallDistance every tick so the damage
 *       check never triggers (client-only fallback).</li>
 * </ul>
 */
public class NoFall extends Module {

    public enum NoFallMode {
        PACKET, PREDICT, SPOOFGROUND, NOGROUND
    }

    private final EnumSetting<NoFallMode> mode = register(new EnumSetting<>(
            "Mode", "Method used to prevent fall damage", NoFallMode.PACKET));

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        switch (mode.get()) {
            case NOGROUND -> {
                // Zero out fall distance every tick â€” client-side prevention only
                if (mc.player.fallDistance > 0) {
                    mc.player.fallDistance = 0;
                }
            }
            case PREDICT -> {
                // Predict ground impact 1 tick early
                if (!mc.player.isOnGround() && mc.player.getVelocity().y < 0) {
                    double nextY      = mc.player.getY() + mc.player.getVelocity().y;
                    BlockPos belowPos = new BlockPos(
                            (int) Math.floor(mc.player.getX()),
                            (int) Math.floor(nextY) - 1,
                            (int) Math.floor(mc.player.getZ()));

                    boolean solidBelow = mc.player.getWorld() != null
                            && !mc.player.getWorld().getBlockState(belowPos).isAir();

                    if (solidBelow && mc.player.fallDistance > 1.5) {
                        sendGroundPacket();
                    }
                }
            }
            case SPOOFGROUND -> {
                // Always send onGround=true while falling
                if (!mc.player.isOnGround() && mc.player.getVelocity().y < 0
                        && mc.player.fallDistance > 0) {
                    sendGroundPacket();
                }
            }
            default -> { /* PACKET mode handled in onMove */ }
        }
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mode.get() != NoFallMode.PACKET) return;

        // Send onGround packet when fall damage would apply (fallDistance > 2 and falling fast)
        if (!mc.player.isOnGround()
                && mc.player.getVelocity().y < -0.1
                && mc.player.fallDistance > 2.0) {
            sendGroundPacket();
            mc.player.fallDistance = 0;
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private void sendGroundPacket() {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
    }
}
