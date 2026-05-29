package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.InventoryUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoBucket extends Module {

    private final DoubleSetting fallThreshold = register(new DoubleSetting(
            "FallHeight", "Minimum fall distance to trigger water placement", 5.0, 2.0, 20.0));

    private final TimerUtil cooldown = new TimerUtil();
    private boolean triggered = false;

    public AutoBucket() {
        super("AutoBucket", "Auto-places water bucket to break fall damage when falling far", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        triggered = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Reset trigger when player lands
        if (mc.player.isOnGround()) {
            triggered = false;
            return;
        }

        // Only trigger once per fall; check fall distance
        if (triggered) return;
        if (mc.player.fallDistance < fallThreshold.get()) return;
        if (!cooldown.hasReached(2000)) return;

        // Find water bucket in hotbar
        int slot = InventoryUtil.findItem(Items.WATER_BUCKET);
        if (slot < 0 || slot >= 9) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prevSlot;

        triggered = true;
        cooldown.reset();
    }
}
