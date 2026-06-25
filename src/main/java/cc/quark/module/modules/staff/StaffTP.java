package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

public class StaffTP extends Module {

    private final StringSetting target = register(new StringSetting(
            "Target", "Name of the player to teleport to", ""));
    private final DoubleSetting offsetX = register(new DoubleSetting(
            "Offset X", "X offset applied after teleport", 3.0, -10.0, 10.0));
    private final DoubleSetting offsetY = register(new DoubleSetting(
            "Offset Y", "Y offset applied after teleport", 0.0, -5.0, 10.0));
    private final DoubleSetting offsetZ = register(new DoubleSetting(
            "Offset Z", "Z offset applied after teleport", 3.0, -10.0, 10.0));
    private final BoolSetting useCommand = register(new BoolSetting(
            "Use Command", "Send server-side /tp command in addition to client-side move", false));
    private final BoolSetting vanishMode = register(new BoolSetting(
            "Vanish Mode", "Apply offset so you appear behind the player, out of sight", true));

    public StaffTP() {
        super("StaffTP", "Teleports to any player with configurable offset", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) { disable(); return; }

        String name = target.get().trim();
        if (name.isEmpty()) {
            ChatUtil.warn("§6[StaffTP] §cSet a Target player name first.");
            disable();
            return;
        }

        PlayerEntity found = null;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p.getName().getString().equalsIgnoreCase(name)) {
                found = p;
                break;
            }
        }

        if (found == null) {
            // Fall back to server-side tp command
            if (mc.player != null) {
                mc.player.networkHandler.sendChatCommand("tp " + mc.player.getName().getString() + " " + name);
                ChatUtil.info("§6[StaffTP] §7Player not visible, sent §f/tp " + name + " §7via command.");
            }
            disable();
            return;
        }

        double tx = found.getX();
        double ty = found.getY();
        double tz = found.getZ();

        double ox = offsetX.get();
        double oy = offsetY.get();
        double oz = offsetZ.get();

        if (vanishMode.isEnabled()) {
            // Position behind the target based on their yaw
            double yaw = Math.toRadians(found.getYaw());
            ox = Math.sin(yaw) * 3.0;
            oz = Math.cos(yaw) * 3.0;
        }

        mc.player.setPosition(tx + ox, ty + oy, tz + oz);

        if (useCommand.isEnabled()) {
            mc.player.networkHandler.sendChatCommand("tp " + name);
        }

        ChatUtil.info("§6[StaffTP] §fTeleported to §e" + found.getName().getString()
                + " §7(" + String.format("%.1f, %.1f, %.1f", tx + ox, ty + oy, tz + oz) + ")");
        disable();
    }
}
