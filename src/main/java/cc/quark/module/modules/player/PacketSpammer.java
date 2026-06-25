package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

import java.time.Instant;

public class PacketSpammer extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "Packet type to spam",
            "Move", "Move", "Swing", "Use"));
    private final IntSetting count = register(new IntSetting(
            "Count", "Packets to send per interval", 5, 1, 50));
    private final IntSetting delayMs = register(new IntSetting(
            "Delay ms", "Delay between spam bursts", 200, 50, 2000));

    private final TimerUtil timer = new TimerUtil();

    public PacketSpammer() {
        super("PacketSpammer", "Spams packets of a configurable type with Count/Delay settings", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!timer.hasReached(delayMs.get())) return;
        timer.reset();

        for (int i = 0; i < count.get(); i++) {
            switch (mode.get()) {
                case "Move" -> {
                    Vec3d pos = mc.player.getPos();
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                            pos.x, pos.y, pos.z, mc.player.isOnGround()));
                }
                case "Swing" ->
                    mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
                case "Use" -> {
                    if (mc.interactionManager != null) {
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    }
                }
            }
        }
    }

    @Override
    public String getSuffix() {
        return mode.get() + " x" + count.get();
    }
}
