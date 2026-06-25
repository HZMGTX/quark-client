package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class GappleAura extends Module {

    private final IntSetting hpThreshold = register(new IntSetting(
            "HP Threshold", "Eat a gapple when health drops below this value", 8, 1, 20));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public GappleAura() {
        super("GappleAura", "Auto-eats gapple when health is critical while fighting", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;
        if (mc.player.getHealth() > hpThreshold.get()) return;
        if (!nearEnemy()) return;

        int slot = findGappleSlot();
        if (slot == -1) return;

        if (mc.player.getInventory().selectedSlot != slot) {
            prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        timer.reset();
    }

    private boolean nearEnemy() {
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(entity) <= 8.0) return true;
        }
        return false;
    }

    private int findGappleSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
        }
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.GOLDEN_APPLE)) return i;
        }
        return -1;
    }
}
