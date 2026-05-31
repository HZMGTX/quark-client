package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

public class NoCrouchAnim extends Module {

    private final BoolSetting noPose = register(new BoolSetting(
            "NoPose", "Suppress the crouching pose change sent to other players", true));

    public NoCrouchAnim() {
        super("NoCrouchAnim", "Removes the crouching animation from third-person view", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (!noPose.isEnabled()) return;
        if (mc.player.isSneaking()) {
            mc.player.setSneaking(false);
        }
    }
}
