package cc.quark.module.modules.player;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketReceive;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

public class PacketLogger extends Module {

    private final BoolSetting incoming = register(new BoolSetting("Incoming", "Log incoming packets", true));
    private final BoolSetting outgoing = register(new BoolSetting("Outgoing", "Log outgoing packets", true));
    private final ModeSetting filter = register(new ModeSetting("Filter", "Packet filter", "All", "All", "Movement", "Combat", "World"));

    public PacketLogger() {
        super("PacketLogger", "Logs all packets for debugging", Category.PLAYER, 0);
    }

    @Override
    public void onEnable() {
        Quark.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        Quark.getInstance().getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (!outgoing.getValue()) return;
        String name = event.getPacket().getClass().getSimpleName();
        if (shouldLog(name)) {
            Quark.LOGGER.info("[PacketLogger] OUT: {}", name);
        }
    }

    @EventHandler
    public void onPacketReceive(EventPacketReceive event) {
        if (!incoming.getValue()) return;
        String name = event.getPacket().getClass().getSimpleName();
        if (shouldLog(name)) {
            Quark.LOGGER.info("[PacketLogger] IN: {}", name);
        }
    }

    private boolean shouldLog(String name) {
        return switch (filter.getValue()) {
            case "Movement" -> name.contains("Move") || name.contains("Position") || name.contains("Velocity");
            case "Combat" -> name.contains("Attack") || name.contains("Damage") || name.contains("Entity");
            case "World" -> name.contains("Block") || name.contains("Chunk") || name.contains("World");
            default -> true;
        };
    }
}
