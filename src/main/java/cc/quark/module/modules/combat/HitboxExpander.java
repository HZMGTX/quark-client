package cc.quark.module.modules.combat;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class HitboxExpander extends Module {

    private final DoubleSetting expand = register(new DoubleSetting("Expand", "Amount to expand entity hitboxes by", 0.2, 0.0, 2.0));
    private final BoolSetting onlyPlayers = register(new BoolSetting("Only Players", "Only expand player hitboxes", true));

    public HitboxExpander() {
        super("HitboxExpander", "Expands entity hitboxes for easier hits", Category.COMBAT);
    }

    /**
     * Returns the expansion amount. Used by a mixin on Entity#getBoundingBox()
     * to inflate the returned box client-side.
     */
    public double getExpand() {
        return isEnabled() ? expand.get() : 0.0;
    }

    public boolean isOnlyPlayers() {
        return onlyPlayers.isEnabled();
    }
}
