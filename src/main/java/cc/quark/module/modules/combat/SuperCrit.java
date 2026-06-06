package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;

public class SuperCrit extends Module {
    private final ModeSetting mode = register(new ModeSetting("Mode", "Crit method", "Jump", "Jump", "Packet", "Cancel"));
    private final BoolSetting onlyFull = register(new BoolSetting("OnlyFull", "Only crit at full cooldown", true));

    public SuperCrit() { super("SuperCrit", "Guarantees critical hits on every attack", Category.COMBAT); }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (onlyFull.getValue() && mc.player.getAttackCooldownProgress(0f) < 0.9f) return;
        if ("Jump".equals(mode.getValue()) && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }
}
