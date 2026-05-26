package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;

/**
 * GuiMove - allows movement input to pass through while a GUI is open.
 */
public class GuiMove extends Module {

    private final BoolSetting jump = register(new BoolSetting("Jump", "Allow jumping in GUIs", true));

    public GuiMove() {
        super("GuiMove", "Lets you move while a screen is open", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        // Movement key forwarding handled by keyboard mixin.
    }
}
