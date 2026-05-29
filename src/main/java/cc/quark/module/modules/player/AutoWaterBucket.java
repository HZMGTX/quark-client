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

public class AutoWaterBucket extends Module {

    private final DoubleSetting fallHeight = register(new DoubleSetting(
            "FallHeight", "Minimum fall distance before placing water", 5.0, 2.0, 20.0));

    private final TimerUtil cooldown = new TimerUtil();
    private boolean triggered = false;
    private int prevSlot = -1;

    public AutoWaterBucket() {
        super("AutoWaterBucket", "Places water bucket when falling more than FallHeight blocks", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        triggered = false;
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        // Restore slot if we switched
        if (prevSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Reset when landing
        if (mc.player.isOnGround()) {
            triggered = false;
            if (prevSlot >= 0) {
                mc.player.getInventory().selectedSlot = prevSlot;
                prevSlot = -1;
            }
            return;
        }

        // One-shot per fall, check fall distance
        if (triggered) return;
        if (mc.player.fallDistance < fallHeight.get()) return;
        if (!cooldown.hasReached(1500)) return;

        int slot = InventoryUtil.findItem(Items.WATER_BUCKET);
        if (slot < 0 || slot >= 9) return;

        if (prevSlot < 0) prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        triggered = true;
        cooldown.reset();
    }
}
