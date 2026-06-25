package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;

public class AdminTP extends Module {

    private final DoubleSetting targetX = register(new DoubleSetting(
            "Target X", "X coordinate to teleport to", 0.0, -30000000.0, 30000000.0));
    private final DoubleSetting targetY = register(new DoubleSetting(
            "Target Y", "Y coordinate to teleport to", 64.0, -64.0, 320.0));
    private final DoubleSetting targetZ = register(new DoubleSetting(
            "Target Z", "Z coordinate to teleport to", 0.0, -30000000.0, 30000000.0));
    private final BoolSetting silent = register(new BoolSetting(
            "Silent", "Client-side only teleport without sending /tp command", true));
    private final BoolSetting sendCommand = register(new BoolSetting(
            "Send Command", "Also send server-side /tp command", false));
    private final BoolSetting keepY = register(new BoolSetting(
            "Keep Y", "Keep current Y level instead of using Target Y", false));

    public AdminTP() {
        super("AdminTP", "Teleports silently to any coordinates", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }

        double x = targetX.get();
        double y = keepY.isEnabled() ? mc.player.getY() : targetY.get();
        double z = targetZ.get();

        if (silent.isEnabled()) {
            mc.player.setPosition(x, y, z);
            ChatUtil.info(String.format("§6[AdminTP] §fTeleported to §e%.1f, %.1f, %.1f §7(client-side)", x, y, z));
        }

        if (sendCommand.isEnabled() && mc.player.networkHandler != null) {
            mc.player.networkHandler.sendChatCommand(
                    String.format("tp %s %.2f %.2f %.2f",
                            mc.player.getName().getString(), x, y, z));
            ChatUtil.info("§6[AdminTP] §7Sent /tp command to server.");
        }

        disable();
    }
}
