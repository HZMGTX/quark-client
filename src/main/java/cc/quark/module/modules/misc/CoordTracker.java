package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import cc.quark.waypoint.WaypointManager;
import org.lwjgl.glfw.GLFW;

public class CoordTracker extends Module {

    private final StringSetting label = register(new StringSetting(
            "Label", "Default label prefix for saved coordinates", "Point"));

    private final IntSetting saveKey = register(new IntSetting(
            "Save Key", "GLFW key code to save current position (default INSERT=260)", GLFW.GLFW_KEY_INSERT, 0, 400));

    private final IntSetting listKey = register(new IntSetting(
            "List Key", "GLFW key code to list saved waypoints in chat (default HOME=268)", GLFW.GLFW_KEY_HOME, 0, 400));

    private final IntSetting clearKey = register(new IntSetting(
            "Clear Key", "GLFW key code to clear all saved waypoints (default DELETE=261)", GLFW.GLFW_KEY_DELETE, 0, 400));

    private int counter = 1;

    public CoordTracker() {
        super("CoordTracker", "Saves coordinates with labels to the waypoint list via key presses", Category.MISC);
    }

    @Override
    public void onEnable() {
        counter = 1;
        ChatUtil.info("CoordTracker: press " + saveKey.get() + " to save, "
                + listKey.get() + " to list, " + clearKey.get() + " to clear.");
    }

    @EventHandler
    public void onKey(EventKey event) {
        if (mc.player == null) return;
        Quark q = Quark.getInstance();
        if (q == null) return;
        WaypointManager wm = q.getWaypointManager();
        if (wm == null) return;

        int key = event.getKeyCode();

        if (key == saveKey.get()) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            String name = label.get() + "_" + counter++;
            wm.add(name, x, y, z);
            ChatUtil.success("Saved waypoint §e" + name
                    + " §7at §f" + String.format("%.0f %.0f %.0f", x, y, z));

        } else if (key == listKey.get()) {
            var list = wm.getWaypoints();
            if (list.isEmpty()) {
                ChatUtil.warn("No waypoints saved.");
            } else {
                ChatUtil.info("§eSaved waypoints (" + list.size() + "):");
                for (var w : list) {
                    ChatUtil.info("  §e" + w.name() + " §7- §f"
                            + String.format("%.0f %.0f %.0f", w.x(), w.y(), w.z()));
                }
            }

        } else if (key == clearKey.get()) {
            int count = wm.getWaypoints().size();
            wm.clear();
            counter = 1;
            ChatUtil.warn("Cleared " + count + " waypoint(s).");
        }
    }
}
