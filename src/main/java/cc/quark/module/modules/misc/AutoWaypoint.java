package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class AutoWaypoint extends Module {

    private final BoolSetting onDeath = register(new BoolSetting("OnDeath", "Create waypoint on death", true));
    private final BoolSetting onBed = register(new BoolSetting("OnBed", "Create waypoint when setting bed", true));
    private final BoolSetting onPortal = register(new BoolSetting("OnPortal", "Create waypoint near portals", true));

    private final List<String> waypoints = new ArrayList<>();
    private boolean wasAlive = true;

    public AutoWaypoint() {
        super("AutoWaypoint", "Automatically creates waypoints at deaths and key locations", Category.MISC);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        boolean alive = mc.player.getHealth() > 0;
        if (wasAlive && !alive && onDeath.getValue()) {
            BlockPos pos = mc.player.getBlockPos();
            String wp = "Death @ " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
            waypoints.add(wp);
            ChatUtil.info("[Waypoint] " + wp);
        }
        wasAlive = alive;
    }

    public List<String> getWaypoints() { return waypoints; }
}
