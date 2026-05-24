package com.ghostclient.module.modules.world;

import com.ghostclient.GhostClient;
import com.ghostclient.event.EventHandler;
import com.ghostclient.event.events.EventAttack;
import com.ghostclient.module.Category;
import com.ghostclient.module.Module;
import com.ghostclient.setting.BoolSetting;
import com.ghostclient.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

public class MiddleClick extends Module {

    private final BoolSetting friends = register(new BoolSetting("Friends", "Add/remove friends on middle click", true));

    public MiddleClick() {
        super("MiddleClick", "Middle click to add/remove friends", Category.WORLD, 0);
    }

    @Override
    public void onEnable() {
        GhostClient.getInstance().getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        GhostClient.getInstance().getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!(event.getTarget() instanceof PlayerEntity player)) return;
        if (!friends.getValue()) return;
        String name = player.getName().getString();
        var fm = GhostClient.getInstance().getFriendManager();
        if (fm.isFriend(name)) {
            fm.removeFriend(name);
            ChatUtil.info("Removed " + name + " from friends.");
        } else {
            fm.addFriend(name);
            ChatUtil.success("Added " + name + " to friends.");
        }
        event.cancel();
    }
}
