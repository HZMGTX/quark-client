package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;

public class NoFall extends Module {

    public enum NoFallMode {
        PACKET, PREDICT, SPOOFGROUND, NOGROUND
    }

    private final EnumSetting<NoFallMode> mode = register(new EnumSetting<>(
            "Mode", "Method used to prevent fall damage", NoFallMode.PACKET));

    private final BoolSetting checkHeight = register(new BoolSetting(
            "Check Height", "Only activate when fall distance would cause damage (>3 blocks)", false));

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        switch (mode.get()) {
            case NOGROUND -> {
                if (mc.player.fallDistance > 0) {
                    mc.player.fallDistance = 0;
                }
            }
            case PREDICT -> {
                if (!mc.player.isOnGround() && mc.player.getVelocity().y < 0) {
                    double nextY = mc.player.getY() + mc.player.getVelocity().y;
                    BlockPos belowPos = new BlockPos(
                            (int) Math.floor(mc.player.getX()),
                            (int) Math.floor(nextY) - 1,
                            (int) Math.floor(mc.player.getZ()));

                    boolean solidBelow = mc.player.getWorld() != null
                            && !mc.player.getWorld().getBlockState(belowPos).isAir();

                    float minDist = checkHeight.isEnabled() ? 3.0f : 1.5f;
                    if (solidBelow && mc.player.fallDistance > minDist) {
                        sendGroundPacket();
                    }
                }
            }
            case SPOOFGROUND -> {
                if (!mc.player.isOnGround() && mc.player.getVelocity().y < 0
                        && mc.player.fallDistance > 0) {
                    float minDist = checkHeight.isEnabled() ? 3.0f : 0.0f;
                    if (mc.player.fallDistance > minDist) {
                        sendGroundPacket();
                    }
                }
            }
            default -> { /* PACKET mode handled in onMove */ }
        }
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mode.get() != NoFallMode.PACKET) return;

        float minDist = checkHeight.isEnabled() ? 3.0f : 2.0f;

        if (!mc.player.isOnGround()
                && mc.player.getVelocity().y < -0.1
                && mc.player.fallDistance > minDist) {
            sendGroundPacket();
            mc.player.fallDistance = 0;
        }
    }

    private void sendGroundPacket() {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(), true));
    }
}
