package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.client.network.PlayerListEntry;

public class UnfreezeAll extends Module {
    private final ModeSetting command = register(new ModeSetting("Command", "Unfreeze command", "unfreeze", "unfreeze", "thaw", "unban-temp"));

    public UnfreezeAll() {
        super("Unfreeze All", "Unfreezes all players on the server", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.getNetworkHandler() == null) { disable(); return; }
        int count = 0;
        for (PlayerListEntry entry : mc.getNetworkHandler().getPlayerList()) {
            String name = entry.getProfile().getName();
            if (!name.equals(mc.player.getName().getString())) {
                mc.player.networkHandler.sendChatCommand(command.get() + " " + name);
                count++;
            }
        }
        ChatUtil.info("[UnfreezeAll] Sent " + command.get() + " to " + count + " players.");
        disable();
    }
}
