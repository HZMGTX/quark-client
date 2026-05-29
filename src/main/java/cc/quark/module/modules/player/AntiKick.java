package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class AntiKick extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Action used to prevent kick",
            "Swing", "Swing", "Move", "KeepAlive"));
    private final DoubleSetting intervalSec = register(new DoubleSetting(
            "Interval", "Seconds between anti-kick actions", 10.0, 1.0, 60.0));

    private final TimerUtil timer = new TimerUtil();

    public AntiKick() {
        super("AntiKick", "Prevents idle kicks by sending periodic activity", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!timer.hasReached((long) (intervalSec.get() * 1000.0))) return;
        timer.reset();

        switch (mode.get()) {
            case "Swing" ->
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            case "Move" -> {
                // Send a tiny position packet with no actual movement
                Vec3d pos = mc.player.getPos();
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                        pos.x, pos.y, pos.z, mc.player.isOnGround()));
            }
            case "KeepAlive" ->
                // Swing as fallback — keepalive is handled elsewhere
                mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
