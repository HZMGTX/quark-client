package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.friend.FriendManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class MiddleClickFriend extends Module {

    private boolean wasMiddleDown = false;

    public MiddleClickFriend() {
        super("MiddleClickFriend", "Middle-click a player to add or remove them as a friend", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.getWindow() == null) return;

        boolean middleDown = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE)
                             == GLFW.GLFW_PRESS;

        if (middleDown && !wasMiddleDown) {
            if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                EntityHitResult ehr = (EntityHitResult) mc.crosshairTarget;
                if (ehr.getEntity() instanceof PlayerEntity player) {
                    String name = player.getName().getString();
                    FriendManager fm = cc.quark.Quark.getInstance().getFriendManager();
                    if (fm.isFriend(name)) {
                        fm.removeFriend(name);
                        ChatUtil.info("Removed " + name + " from friends.");
                    } else {
                        fm.addFriend(name);
                        ChatUtil.info("Added " + name + " to friends.");
                    }
                }
            }
        }
        wasMiddleDown = middleDown;
    }
}
