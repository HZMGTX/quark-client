package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class PlayerKicker extends Module {
    private final StringSetting target = register(new StringSetting("Target", "Player to kick", ""));
    private final StringSetting reason = register(new StringSetting("Reason", "Kick reason", "Kicked by admin"));
    private final BoolSetting useBan = register(new BoolSetting("Ban", "Ban instead of kick", false));
    private boolean executed = false;

    public PlayerKicker() { super("PlayerKicker", "Kicks/bans a player via server command", Category.STAFF); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); executed = false; }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || executed || target.get().isEmpty()) return;
        String cmd = useBan.isEnabled() ? "ban " + target.get() + " " + reason.get() : "kick " + target.get() + " " + reason.get();
        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.info("Executed: /" + cmd);
        executed = true;
        disable();
    }
}
