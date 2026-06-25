package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class AutoSprint2 extends Module {
    private final BoolSetting inCombat = register(new BoolSetting("In Combat", "Sprint in combat mode", true));
    private final BoolSetting stopOnHit = register(new BoolSetting("Stop On Hit", "Stop sprint when taking damage", false));

    public AutoSprint2() { super("AutoSprint2", "Enhanced auto-sprint with combat awareness", Category.PLAYER); }
    @Override public void onDisable() { if (mc.player != null) mc.player.setSprinting(false); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        if (stopOnHit.isEnabled() && mc.player.hurtTime > 0) { mc.player.setSprinting(false); return; }
        boolean moving = mc.options.forwardKey.isPressed();
        if (moving && (inCombat.isEnabled() || !mc.player.isSprinting())) mc.player.setSprinting(true);
    }
}
