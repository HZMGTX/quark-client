package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;

public class Reach extends Module {

    /** Read by the companion mixin to override the attack-range check. */
    public static double currentReach = 3.0;
    public static boolean onlyPlayersMode = false;

    private final DoubleSetting reach = register(new DoubleSetting(
            "Reach", "Maximum attack range in blocks", 3.5, 3.0, 6.0));

    private final BoolSetting onlyPlayers = register(new BoolSetting(
            "Only Players", "Apply extended reach only when attacking player entities", false));

    public Reach() {
        super("Reach", "Extends the player attack range beyond the vanilla 3-block limit", Category.COMBAT);
    }

    @Override
    public String getSuffix() {
        return String.format("%.1f", reach.get());
    }

    @Override
    public void onEnable() {
        updateStatics();
    }

    @Override
    public void onDisable() {
        currentReach = 3.0;
        onlyPlayersMode = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        updateStatics();
    }

    private void updateStatics() {
        currentReach = reach.get();
        onlyPlayersMode = onlyPlayers.isEnabled();
    }
}
