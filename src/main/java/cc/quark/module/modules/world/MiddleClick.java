package cc.quark.module.modules.world;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;

public class MiddleClick extends Module {

    private final BoolSetting friends = register(new BoolSetting("Friends", "Add/remove friends on middle click", true));

    public MiddleClick() {
        super("MiddleClick", "Middle click to add/remove friends", Category.WORLD);
    }

    

    

    @EventHandler
    public void onAttack(EventAttack event) {
        if (!(event.getTarget() instanceof PlayerEntity player)) return;
        if (!friends.getValue()) return;
        String name = player.getName().getString();
        var fm = Quark.getInstance().getFriendManager();
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
