package cc.quark.module.modules.staff;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;

public class StaffMode extends Module {
    private final BoolSetting autoVanish = register(new BoolSetting("Auto Vanish", "Enable vanish on staff mode", true));
    private final BoolSetting autoFly = register(new BoolSetting("Auto Fly", "Enable flight on staff mode", true));
    private final BoolSetting noClip = register(new BoolSetting("NoClip", "Phase through blocks", false));

    public StaffMode() {
        super("Staff Mode", "Toggle full staff loadout (vanish, fly, ESP)", Category.STAFF, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) { disable(); return; }
        ChatUtil.info("§6[StaffMode] §fActivated — vanish/fly/ESP enabled");
        if (autoVanish.isEnabled()) {
            mc.player.setInvisible(true);
        }
        if (autoFly.isEnabled()) {
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().allowFlying = true;
            mc.player.sendAbilitiesUpdate();
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.setInvisible(false);
        mc.player.getAbilities().flying = false;
        mc.player.getAbilities().allowFlying = false;
        mc.player.sendAbilitiesUpdate();
        ChatUtil.info("§6[StaffMode] §fDeactivated");
    }
}
