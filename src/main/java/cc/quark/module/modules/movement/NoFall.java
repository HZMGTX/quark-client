package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventMove;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.EnumSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;

public class NoFall extends Module {

    public enum NoFallMode {
        PACKET, SPOOF, LEGIT
    }

    private final EnumSetting<NoFallMode> mode = register(new EnumSetting<>(
            "Mode", "Method used to prevent fall damage", NoFallMode.PACKET));

    private final DoubleSetting minHeight = register(new DoubleSetting(
            "Min Height", "Minimum fall distance before triggering (blocks)", 3.0, 3.0, 20.0));

    // Legit mode: track if we need to jump before landing
    private boolean legitArmed = false;

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        legitArmed = false;
    }

    @Override
    public String getSuffix() {
        return mode.get().name();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        switch (mode.get()) {

            // --------------------------------------------------------- SPOOF
            // Set onGround = true every tick — risky but bypasses simple checks
            case SPOOF -> {
                if (mc.player.fallDistance > minHeight.get()) {
                    mc.player.setOnGround(true);
                }
            }

            // --------------------------------------------------------- LEGIT
            // Predict landing and jump just before touching ground to cancel fall
            case LEGIT -> {
                if (!mc.player.isOnGround() && mc.player.getVelocity().y < 0) {
                    double vy     = mc.player.getVelocity().y;
                    double nextY  = mc.player.getY() + vy;

                    BlockPos belowPos = new BlockPos(
                            (int) Math.floor(mc.player.getX()),
                            (int) Math.floor(nextY) - 1,
                            (int) Math.floor(mc.player.getZ()));

                    boolean solidBelow = mc.world != null
                            && !mc.world.getBlockState(belowPos).isAir();

                    if (solidBelow && mc.player.fallDistance > minHeight.get()) {
                        // Jump slightly before landing to reset fall distance
                        if (!legitArmed) {
                            mc.player.jump();
                            legitArmed = true;
                        }
                    }
                } else if (mc.player.isOnGround()) {
                    legitArmed = false;
                }
            }

            default -> { /* PACKET mode handled in onMove */ }
        }
    }

    @EventHandler
    public void onMove(EventMove event) {
        if (mc.player == null) return;
        if (mode.get() != NoFallMode.PACKET) return;

        // When fall distance exceeds minHeight and we are falling,
        // send a Full packet with onGround=true to reset server-side fall damage.
        if (!mc.player.isOnGround()
                && mc.player.getVelocity().y < -0.1
                && mc.player.fallDistance > minHeight.get()) {
            sendGroundPacket();
            mc.player.fallDistance = 0;
        }
    }

    private void sendGroundPacket() {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.Full(
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        mc.player.getYaw(), mc.player.getPitch(), true));
    }
}
