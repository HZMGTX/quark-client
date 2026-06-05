package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * Glitch - sends alternating on-ground packets each tick to confuse
 * server-side position/ground checks.
 *
 * <p>Sends N pairs of OnGroundOnly(true) / OnGroundOnly(false) per tick.
 * {@code PlayerMoveC2SPacket.OnGroundOnly} requires two arguments:
 * {@code (boolean onGround, boolean horizontalCollision)}.
 */
public class Glitch extends Module {

    private final IntSetting count = register(new IntSetting(
            "Count", "Number of true/false packet pairs to send per tick", 1, 1, 5));

    public Glitch() {
        super("Glitch", "Spam ground-spoof packets to confuse server checks", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;

        boolean hCol = mc.player.horizontalCollision;

        for (int i = 0; i < count.get(); i++) {
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.OnGroundOnly(true));
            mc.getNetworkHandler().sendPacket(
                    new PlayerMoveC2SPacket.OnGroundOnly(false));
        }
    }
}
