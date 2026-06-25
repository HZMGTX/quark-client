package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.ChatUtil;

public class AltManager extends Module {
    private final StringSetting altName = register(new StringSetting("Alt Name", "Alt account display name", ""));
    private final BoolSetting showInHUD = register(new BoolSetting("Show HUD", "Show alt info in HUD", true));

    public AltManager() { super("AltManager", "Manages alt accounts and tracks alt identities", Category.STAFF); }
    @Override public void onEnable() { if (!altName.get().isEmpty()) ChatUtil.info("Managing alt: " + altName.get()); }

    @EventHandler
    public void onTick(EventTick e) { /* Alt switching requires auth API integration */ }
}
