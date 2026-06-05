package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class SummonPlayer extends Module {
    private final StringSetting target = register(new StringSetting("Target", "Player name to summon", ""));
    private final BoolSetting broadcast = register(new BoolSetting("Announce", "Announce teleport in chat", false));

    public SummonPlayer() {
        super("Summon Player", "Teleport a player to your location", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        String name = target.get().trim();
        if (name.isEmpty()) { ChatUtil.warn("[Summon] Set a target name."); disable(); return; }

        mc.player.networkHandler.sendChatCommand("tp " + name + " " + mc.player.getName().getString());
        ChatUtil.info("[Summon] Summoned " + name);
        if (broadcast.isEnabled()) {
            mc.player.networkHandler.sendChatMessage("Teleported " + name + " to me.");
        }
        disable();
    }
}
