package cc.quark.module.modules.misc;

import cc.quark.Quark;
import cc.quark.event.EventHandler;
import cc.quark.event.events.EventRender2D;
import cc.quark.friend.FriendManager;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ColorSetting;
import cc.quark.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

public class FriendList extends Module {

    private final BoolSetting showHUD = register(new BoolSetting(
            "Show HUD", "Show online friend count in the corner", true));
    private final ColorSetting friendColor = register(new ColorSetting(
            "Friend Color", "Color for friends in ESP", 0xFF44FFAA));

    public FriendList() {
        super("FriendList", "HUD overlay for the friends list (use .friend to manage)", Category.MISC);
    }

    private FriendManager fm() {
        return Quark.getInstance().getFriendManager();
    }

    public int getFriendColor() {
        return friendColor.get();
    }

    @Override
    public String getSuffix() {
        FriendManager fm = fm();
        return fm != null ? fm.getFriends().size() + " friends" : "0 friends";
    }

    @EventHandler
    public void onRender2D(EventRender2D event) {
        if (!showHUD.isEnabled() || mc.player == null) return;
        FriendManager fm = fm();
        if (fm == null) return;

        DrawContext ctx = event.getDrawContext();
        int online = 0;
        int total = fm.getFriends().size();
        if (mc.getNetworkHandler() != null) {
            for (var entry : mc.getNetworkHandler().getPlayerList()) {
                if (fm.isFriend(entry.getProfile().getName())) online++;
            }
        }
        String text = "§7Friends: §a" + online + "§7/§f" + total;
        RenderUtil.drawCustomText(ctx, text, 2, 2, 0xFFFFFFFF);
    }
}
