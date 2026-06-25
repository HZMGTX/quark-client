package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoMLG extends Module {
    private final IntSetting fallHeight = register(new IntSetting("Fall Height", "Fall height to trigger MLG", 10, 3, 30));

    public AutoMLG() { super("AutoMLG", "Auto-places water bucket on fall damage", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.player.isOnGround() || mc.player.isTouchingWater()) return;
        if (mc.player.getVelocity().y >= -0.5) return;
        double fallDist = mc.player.fallDistance;
        if (fallDist < fallHeight.get()) return;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.WATER_BUCKET) {
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                return;
            }
        }
    }
}
