package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.InventoryUtil;

/**
 * BestTool - swaps to the best pickaxe when mining a block.
 */
public class BestTool extends Module {

    private final BoolSetting onlyMining = register(new BoolSetting("OnlyMining", "Only when breaking", true));

    public BestTool() {
        super("BestTool", "Picks the best pickaxe automatically", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        if (onlyMining.isEnabled() && !mc.options.attackKey.isPressed()) return;
        int slot = InventoryUtil.findBestPickaxe();
        if (slot >= 0 && slot < 9) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }
}
