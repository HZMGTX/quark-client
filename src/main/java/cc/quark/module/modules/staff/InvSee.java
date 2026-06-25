package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class InvSee extends Module {

    private final StringSetting target = register(new StringSetting(
            "Target", "Player name whose inventory to inspect", ""));

    public InvSee() {
        super("InvSee", "Silently view and edit player inventories.", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            disable();
            return;
        }

        String playerName = target.get().trim();
        if (playerName.isEmpty()) {
            ChatUtil.error("[InvSee] Set the Target setting to a player name first.");
            disable();
            return;
        }

        // Send the /invsee command via the chat command handler
        mc.player.networkHandler.sendChatCommand("invsee " + playerName);

        // Disable immediately - this is a one-shot action
        disable();
    }
}
