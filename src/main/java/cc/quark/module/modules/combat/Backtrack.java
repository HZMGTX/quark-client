package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;

import java.util.ArrayList;
import java.util.List;

public class Backtrack extends Module {

    private final IntSetting delayMs = register(new IntSetting(
            "Delay Ms", "How many milliseconds to delay entity position updates", 100, 0, 500));

    private record PendingPacket(long receiveTime, Packet<?> packet) {}

    private final List<PendingPacket> queue = new ArrayList<>();

    public Backtrack() {
        super("Backtrack", "Delays entity position rendering to aid hit registration", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        mc.execute(() -> queue.clear());
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (mc.player == null || delayMs.get() <= 0) return;

        Packet<?> pkt = event.getPacket();
        boolean isPos = (pkt instanceof EntityPositionS2CPacket)
                || (pkt instanceof EntityS2CPacket);
        if (!isPos) return;

        mc.execute(() -> queue.add(new PendingPacket(System.currentTimeMillis(), pkt)));
        event.setCancelled(true);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        mc.execute(() -> {
            List<PendingPacket> toRelease = new ArrayList<>();
            for (PendingPacket pp : queue) {
                if (now - pp.receiveTime() >= delayMs.get()) {
                    toRelease.add(pp);
                }
            }
            queue.removeAll(toRelease);
        });
    }
}
