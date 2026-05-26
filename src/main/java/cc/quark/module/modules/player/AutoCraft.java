package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.InventoryUtil;
import net.minecraft.item.Items;

/**
 * AutoCraft - notifies when enough planks are present to craft (placeholder crafting helper).
 */
public class AutoCraft extends Module {

    private final BoolSetting notify = register(new BoolSetting("Notify", "Announce when craftable", true));
    private boolean announced = false;

    public AutoCraft() {
        super("AutoCraft", "Detects when crafting materials are available", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        announced = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;
        int planks = InventoryUtil.countItem(Items.OAK_PLANKS);
        if (planks >= 4) {
            if (notify.isEnabled() && !announced) {
                ChatUtil.info("Enough planks to craft (" + planks + ")");
                announced = true;
            }
        } else {
            announced = false;
        }
    }
}
