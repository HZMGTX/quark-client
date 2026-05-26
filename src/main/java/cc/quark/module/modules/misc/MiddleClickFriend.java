package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;

public class MiddleClickFriend extends Module {

    private final Set<String> friends = new HashSet<>();

    public MiddleClickFriend() {
        super("MiddleClickFriend", "Middle-click players to add/remove them as friends", Category.MISC);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.options.pickItemKey.isPressed()) return;

        if (mc.targetedEntity instanceof PlayerEntity target) {
            String name = target.getName().getString();
            if (friends.contains(name)) {
                friends.remove(name);
                mc.player.sendMessage(net.minecraft.text.Text.literal("§cRemoved " + name + " from friends"), true);
            } else {
                friends.add(name);
                mc.player.sendMessage(net.minecraft.text.Text.literal("§aAdded " + name + " to friends"), true);
            }
        }
    }

    public boolean isFriend(String name) { return friends.contains(name); }
    public Set<String> getFriends() { return friends; }
}
