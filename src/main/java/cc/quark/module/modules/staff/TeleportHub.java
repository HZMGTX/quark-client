package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class TeleportHub extends Module {
    private final StringSetting hubCommand = register(new StringSetting("Hub Cmd", "Command to reach hub", "hub"));
    private final StringSetting lobbyCommand = register(new StringSetting("Lobby Cmd", "Command for lobby", "lobby"));
    private final ModeSetting destination = register(new ModeSetting("Dest", "Where to go", "Hub", "Hub", "Lobby", "Spawn"));
    private boolean executed = false;

    public TeleportHub() { super("TeleportHub", "Teleports to server hub or lobby", Category.STAFF); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); executed = false; }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || executed) return;
        String cmd = switch (destination.get()) {
            case "Lobby" -> lobbyCommand.get();
            case "Spawn" -> "spawn";
            default -> hubCommand.get();
        };
        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.info("Sent to " + destination.get());
        executed = true;
        disable();
    }
}
