package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.friend.FriendManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class MiddleClickFriend extends Module {

    private final BoolSetting showNotification = register(new BoolSetting(
            "Notify", "Show chat notification on friend add/remove", true));

    private boolean wasMiddleDown = false;

    public MiddleClickFriend() {
        super("MiddleClickFriend", "Middle-click a player entity to add or remove them from your friends list", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getWindow() == null) return;

        long handle = mc.getWindow().getHandle();
        boolean middleDown = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        if (middleDown && !wasMiddleDown) {
            tryToggleFriend();
        }
        wasMiddleDown = middleDown;
    }

    private void tryToggleFriend() {
        if (mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult ehr = (EntityHitResult) mc.crosshairTarget;
        if (!(ehr.getEntity() instanceof PlayerEntity player)) return;

        String name = player.getName().getString();
        FriendManager fm = cc.quark.Quark.getInstance().getFriendManager();

        if (fm.isFriend(name)) {
            fm.removeFriend(name);
            if (showNotification.isEnabled()) ChatUtil.info("§cRemoved §r" + name + "§7 from friends.");
        } else {
            fm.addFriend(name);
            if (showNotification.isEnabled()) ChatUtil.info("§aAdded §r" + name + "§7 to friends.");
        }
    }
}
