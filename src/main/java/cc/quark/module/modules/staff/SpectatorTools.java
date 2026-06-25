package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.event.events.EventKey;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.Iterator;

public class SpectatorTools extends Module {
    private final BoolSetting quickSwitch = register(new BoolSetting("QuickSwitch", "Arrow keys switch spectated player", true));
    private final DoubleSetting followRange = register(new DoubleSetting("FollowRange", "Auto-follow range", 100.0, 10.0, 500.0));
    private int playerIndex = 0;

    public SpectatorTools() { super("SpectatorTools", "Enhanced spectator mode tools for staff", Category.STAFF); }

    @EventHandler
    public void onKey(EventKey event) {
        if (!quickSwitch.getValue() || mc.world == null || mc.player == null) return;
        if (event.getKey() == GLFW.GLFW_KEY_RIGHT) {
            var players = mc.world.getPlayers();
            playerIndex = (playerIndex + 1) % Math.max(1, players.size());
            ChatUtil.info("[Spec] Switched to player " + playerIndex);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
    }
}
