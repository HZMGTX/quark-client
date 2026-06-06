package cc.quark.module.modules.staff;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

public class StaffMode extends Module {
    private final BoolSetting vanish = register(new BoolSetting("Vanish", "Enable vanish on activation", true));
    private final BoolSetting flyMode = register(new BoolSetting("Fly", "Enable fly on activation", true));
    private final BoolSetting godMode = register(new BoolSetting("God", "Enable god mode on activation", false));
    private final BoolSetting nightVision = register(new BoolSetting("NightVision", "Enable night vision", true));
    public StaffMode() { super("StaffMode", "Activates full staff toolkit at once", Category.STAFF); }
    @Override
    public void onEnable() {
        if (mc.player == null) return;
        ChatUtil.info("Staff Mode activated.");
    }
    @Override
    public void onDisable() {
        ChatUtil.info("Staff Mode deactivated.");
    }
    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
    }
}
