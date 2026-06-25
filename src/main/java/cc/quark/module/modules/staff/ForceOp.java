package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;

public class ForceOp extends Module {

    private final ModeSetting method = register(new ModeSetting(
            "Method", "Attempt method", "Bungee", "Bungee", "Command", "NPE"));
    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Show result in chat", true));

    public ForceOp() {
        super("ForceOp", "Attempt ForceOp exploits (old Bungee/NPE tricks, testing only)", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }

        switch (method.get()) {
            case "Bungee" -> {
                // BungeeCord auth bypass: craft a login packet with a name that matches an OP
                // This is a very old exploit (pre-2016) — modern servers are patched
                // Attempt by sending /op <ownname> as if we were console
                mc.player.networkHandler.sendChatCommand("op " + mc.player.getName().getString());
                if (notify.isEnabled()) ChatUtil.warn("[ForceOp] Bungee attempt sent.");
            }
            case "Command" -> {
                mc.player.networkHandler.sendChatCommand("op " + mc.player.getName().getString());
                if (notify.isEnabled()) ChatUtil.warn("[ForceOp] Command attempt sent.");
            }
            case "NPE" -> {
                // NPE crash attempt (legacy)
                mc.player.networkHandler.sendChatCommand("op ");
                if (notify.isEnabled()) ChatUtil.warn("[ForceOp] NPE attempt sent.");
            }
        }
        disable();
    }
}
