package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BowItem;

public class BowCharge extends Module {

    private final IntSetting chargePercent = register(new IntSetting("ChargePercent", "Release bow at this charge level (50-100%)", 100, 50, 100));

    public BowCharge() {
        super("BowCharge", "Auto-releases bow at optimal charge level", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!mc.player.isUsingItem()) return;

        var active = mc.player.getActiveItem();
        if (!(active.getItem() instanceof BowItem)) return;

        int elapsed = active.getItem().getMaxUseTime(active, mc.player) - mc.player.getItemUseTimeLeft();
        float currentCharge = BowItem.getPullProgress(elapsed);
        float targetCharge = chargePercent.get() / 100.0f;

        if (currentCharge >= targetCharge) {
            mc.player.stopUsingItem();
        }
    }
}
