package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventChat;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class StaffTeleportMenu extends Module {

    private final StringSetting location1 = register(new StringSetting(
            "Location 1", "Waypoint 1 as x,y,z,world (e.g. 100,64,200,world)", ""));
    private final StringSetting location2 = register(new StringSetting(
            "Location 2", "Waypoint 2 as x,y,z,world", ""));
    private final StringSetting location3 = register(new StringSetting(
            "Location 3", "Waypoint 3 as x,y,z,world", ""));
    private final StringSetting location4 = register(new StringSetting(
            "Location 4", "Waypoint 4 as x,y,z,world", ""));
    private final StringSetting location5 = register(new StringSetting(
            "Location 5", "Waypoint 5 as x,y,z,world", ""));
    private final IntSetting activeSlot = register(new IntSetting(
            "Active Slot", "Which waypoint slot to teleport to (1–5)", 1, 1, 5));

    public StaffTeleportMenu() {
        super("StaffTeleportMenu", "Maintains saved staff teleport waypoints and fires /tp on enable", Category.STAFF);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }

        String loc = getSlot(activeSlot.get());
        if (loc == null || loc.trim().isEmpty()) {
            ChatUtil.warn("[StaffTPMenu] Slot " + activeSlot.get() + " is empty. Type /stwp set <slot> to save.");
            disable();
            return;
        }

        String cmd = buildTpCommand(loc);
        if (cmd == null) {
            ChatUtil.warn("[StaffTPMenu] Invalid format for slot " + activeSlot.get() + ". Use x,y,z or x,y,z,world.");
            disable();
            return;
        }

        mc.player.networkHandler.sendChatCommand(cmd);
        ChatUtil.info("§6[StaffTPMenu] §fTeleporting to slot §e" + activeSlot.get() + " §f-> §e" + loc);
        disable();
    }

    /** Intercept "/stwp set <slot>" chat shortcut to save current position. */
    @EventHandler
    public void onChat(EventChat event) {
        if (mc.player == null || event.isIncoming()) return;
        String msg = event.getMessage();
        if (msg == null || !msg.startsWith("/stwp set ")) return;
        event.cancel();

        try {
            int slot = Integer.parseInt(msg.substring(10).trim());
            if (slot < 1 || slot > 5) { ChatUtil.warn("[StaffTPMenu] Slot must be 1–5."); return; }
            double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
            String val = (int)x + "," + (int)y + "," + (int)z + ",world";
            setSlot(slot, val);
            ChatUtil.success("§6[StaffTPMenu] §fSaved slot §e" + slot + " §fas §e" + val);
        } catch (NumberFormatException e) {
            ChatUtil.warn("[StaffTPMenu] Usage: /stwp set <1-5>");
        }
    }

    private String buildTpCommand(String loc) {
        String[] parts = loc.trim().split(",");
        if (parts.length < 3) return null;
        try {
            int x = Integer.parseInt(parts[0].trim());
            int y = Integer.parseInt(parts[1].trim());
            int z = Integer.parseInt(parts[2].trim());
            return "tp " + mc.player.getName().getString() + " " + x + " " + y + " " + z;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getSlot(int slot) {
        return switch (slot) {
            case 1 -> location1.get();
            case 2 -> location2.get();
            case 3 -> location3.get();
            case 4 -> location4.get();
            case 5 -> location5.get();
            default -> null;
        };
    }

    private void setSlot(int slot, String val) {
        switch (slot) {
            case 1 -> location1.setValue(val);
            case 2 -> location2.setValue(val);
            case 3 -> location3.setValue(val);
            case 4 -> location4.setValue(val);
            case 5 -> location5.setValue(val);
        }
    }
}
