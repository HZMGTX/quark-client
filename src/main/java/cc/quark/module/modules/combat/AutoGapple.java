package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoGapple extends Module {

    private final IntSetting healthThreshold = register(new IntSetting(
            "Health Threshold", "Eat a gapple when health drops at or below this half-heart value", 12, 6, 18));

    private final BoolSetting absorbOnly = register(new BoolSetting(
            "Absorb Only", "Only eat enchanted golden apples (provides absorption)", false));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;
    private boolean eating = false;

    public AutoGapple() {
        super("AutoGapple", "Automatically eats golden apples when health drops below a threshold", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        prevSlot = -1;
        eating = false;
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player != null && prevSlot != -1) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
        prevSlot = -1;
        eating = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(400)) return;

        float health = mc.player.getHealth();
        float threshold = healthThreshold.get();

        if (eating && health > threshold) {
            if (prevSlot != -1) {
                mc.player.getInventory().selectedSlot = prevSlot;
                prevSlot = -1;
            }
            eating = false;
            return;
        }

        if (health > threshold) return;

        int slot = findGappleSlot();
        if (slot == -1) {
            if (eating && prevSlot != -1) {
                mc.player.getInventory().selectedSlot = prevSlot;
                prevSlot = -1;
            }
            eating = false;
            return;
        }

        if (mc.player.getInventory().selectedSlot != slot) {
            if (!eating) prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
        }

        eating = true;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        timer.reset();
    }

    private int findGappleSlot() {
        int regularSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
            if (!absorbOnly.isEnabled() && stack.isOf(Items.GOLDEN_APPLE) && regularSlot == -1) {
                regularSlot = i;
            }
        }
        return regularSlot;
    }
}
