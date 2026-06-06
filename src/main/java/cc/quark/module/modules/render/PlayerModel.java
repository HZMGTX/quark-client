package cc.quark.module.modules.render;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

public class PlayerModel extends Module {
    private final ModeSetting skinMode = register(new ModeSetting("Skin Mode", "How to render player skin", "Default", "Default", "No Overlay", "All Layers"));
    private final BoolSetting hideArmor = register(new BoolSetting("Hide Armor", "Render players without armor visually", false));

    public PlayerModel() { super("PlayerModel", "Modifies how players are rendered visually", Category.RENDER); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        // Model modifications applied through rendering hooks
    }
}
