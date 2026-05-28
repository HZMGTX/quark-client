package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;

public class FastHurt extends Module {

    private final ModeSetting mode = register(new ModeSetting(
            "Mode", "When to remove I-frames",
            "Always",
            "Always", "In Combat"));

    private int combatTimer = 0;

    public FastHurt() {
        super("FastHurt", "Removes invincibility frames so you can be hit faster consecutively", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        combatTimer = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (mc.player.hurtTime > 0) {
            combatTimer = 60;
        }

        if (mode.is("In Combat") && combatTimer <= 0) return;
        if (combatTimer > 0) combatTimer--;

        mc.player.hurtTime = 0;
    }

    @Override
    public String getSuffix() {
        return mode.get();
    }
}
