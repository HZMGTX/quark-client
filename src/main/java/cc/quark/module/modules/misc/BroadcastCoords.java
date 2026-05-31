package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;

public class BroadcastCoords extends Module {

    private final IntSetting intervalSeconds = register(new IntSetting(
            "Interval", "Seconds between each coordinate broadcast", 60, 10, 600));

    private final ModeSetting channel = register(new ModeSetting(
            "Channel", "Where to broadcast",
            "Public", "Public", "Team", "Whisper"));

    private final BoolSetting includeY = register(new BoolSetting(
            "IncludeY", "Include the Y coordinate in the broadcast", true));

    private final BoolSetting includeWorld = register(new BoolSetting(
            "IncludeWorld", "Include current world/dimension name", false));

    private final TimerUtil timer = new TimerUtil();

    public BroadcastCoords() {
        super("BroadcastCoords", "Broadcasts the player's coordinates to chat at regular intervals", Category.MISC);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(intervalSeconds.get() * 1000L)) return;

        int x = (int) mc.player.getX();
        int y = (int) mc.player.getY();
        int z = (int) mc.player.getZ();

        StringBuilder msg = new StringBuilder();
        msg.append("My coords: X: ").append(x);
        if (includeY.isEnabled()) msg.append(", Y: ").append(y);
        msg.append(", Z: ").append(z);

        if (includeWorld.isEnabled() && mc.world.getRegistryKey() != null) {
            String dim = mc.world.getRegistryKey().getValue().getPath();
            msg.append(" [").append(dim).append("]");
        }

        String message = msg.toString();

        switch (channel.get()) {
            case "Public" -> mc.player.networkHandler.sendChatMessage(message);
            case "Team"   -> mc.player.networkHandler.sendChatMessage("/teammsg " + message);
            case "Whisper" -> {
                // Send as /msg to self as a placeholder — player can configure target
                mc.player.networkHandler.sendChatMessage("/msg " + mc.player.getName().getString() + " " + message);
            }
        }

        timer.reset();
    }

    @Override
    public String getSuffix() {
        if (mc.player == null) return "";
        return (int) mc.player.getX() + ", " + (int) mc.player.getZ();
    }
}
