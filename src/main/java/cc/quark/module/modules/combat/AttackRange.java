package cc.quark.module.modules.combat;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class AttackRange extends Module {

    private final DoubleSetting extraRange = register(new DoubleSetting("Extra Range", "Extra blocks added to melee attack range", 0.5, 0.0, 3.0));
    private final BoolSetting onlyPlayers = register(new BoolSetting("Only Players", "Only extend range against players", true));

    public AttackRange() {
        super("AttackRange", "Extends melee attack range slightly", Category.COMBAT);
    }

    /**
     * Returns the total extended range. To be read by a mixin that intercepts
     * the player-reach distance in ClientPlayerInteractionManager.
     */
    public double getExtraRange() {
        return isEnabled() ? extraRange.get() : 0.0;
    }

    public boolean isOnlyPlayers() {
        return onlyPlayers.isEnabled();
    }
}
