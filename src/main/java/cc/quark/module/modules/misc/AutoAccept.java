package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class AutoAccept extends Module {

    private final BoolSetting friends = register(new BoolSetting(
            "Friends", "Auto-accept incoming friend requests", true));

    private final BoolSetting trades = register(new BoolSetting(
            "Trades", "Auto-accept incoming trade requests", true));

    public AutoAccept() {
        super("AutoAccept", "Auto-accepts friend/trade requests", Category.MISC);
    }

    @EventHandler
    public void onChat(EventChat event) {
        if (!event.isIncoming()) return;
        String msg = event.getMessage().toLowerCase();

        if (friends.isEnabled() && (msg.contains("friend request") || msg.contains("sent you a friend"))) {
            sendAccept("/friend accept");
        }

        if (trades.isEnabled() && (msg.contains("trade request") || msg.contains("wants to trade"))) {
            sendAccept("/trade accept");
        }
    }

    private void sendAccept(String command) {
        if (mc.player == null) return;
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.networkHandler.sendChatCommand(command.substring(1));
            }
        });
    }
}
