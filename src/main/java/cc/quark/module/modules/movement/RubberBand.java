package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/**
 * RubberBand - periodically resends the current position to fight lag pulls.
 */
public class RubberBand extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between resyncs", 5, 1, 40));
    private int counter;

    public RubberBand() {
        super("RubberBand", "Resync position to server", Category.MOVEMENT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (++counter < delay.get()) return;
        counter = 0;
        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.isOnGround()));
    }
}
