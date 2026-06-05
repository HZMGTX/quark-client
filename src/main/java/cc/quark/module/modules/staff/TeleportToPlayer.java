package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class TeleportToPlayer extends Module {
    private final StringSetting target = register(new StringSetting("Target", "Player name to teleport to", ""));
    private final BoolSetting useCommand = register(new BoolSetting("Use Command", "Send /tp command", true));
    private final BoolSetting localTP = register(new BoolSetting("Local TP", "Teleport client-side to visible player", false));

    public TeleportToPlayer() {
        super("TP To Player", "Teleport to a player by name", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String name = target.get().trim();
        if (name.isEmpty()) {
            ChatUtil.warn("[TPToPlayer] Set a target name first.");
            disable(); return;
        }

        if (localTP.isEnabled() && mc.world != null) {
            for (var p : mc.world.getPlayers()) {
                if (p.getName().getString().equalsIgnoreCase(name)) {
                    mc.player.setPosition(p.getX(), p.getY(), p.getZ());
                    ChatUtil.info("[TPToPlayer] Teleported to " + name);
                    disable(); return;
                }
            }
            ChatUtil.warn("[TPToPlayer] Player not visible locally.");
        }

        if (useCommand.isEnabled()) {
            mc.player.networkHandler.sendChatCommand("tp " + name);
            ChatUtil.info("[TPToPlayer] Sent /tp " + name);
        }
        disable();
    }
}
