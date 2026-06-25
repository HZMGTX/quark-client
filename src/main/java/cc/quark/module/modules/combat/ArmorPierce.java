package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;

public class ArmorPierce extends Module {
    private final DoubleSetting bonus = register(new DoubleSetting("DmgBonus", "Extra damage multiplier", 1.2, 1.0, 3.0));
    public ArmorPierce() { super("ArmorPierce", "Increases effective damage output", Category.COMBAT); }
    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null || event.getTarget() == null) return;
    }
}
