package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.client.option.KeyBinding;

public class AntiDropItems extends Module {

    private final BoolSetting preventQ = register(new BoolSetting(
            "PreventQ", "Block the drop key (Q) from dropping items", true));
    private final BoolSetting preventDrag = register(new BoolSetting(
            "PreventDrag", "Block drag-dropping items out of inventory", true));

    public AntiDropItems() {
        super("AntiDropItems", "Prevents accidentally dropping items", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        if (preventQ.isEnabled()) {
            KeyBinding dropKey = mc.options.dropKey;
            if (dropKey.isPressed()) {
                KeyBinding.setKeyPressed(dropKey.getDefaultKey(), false);
            }
        }
    }
}
