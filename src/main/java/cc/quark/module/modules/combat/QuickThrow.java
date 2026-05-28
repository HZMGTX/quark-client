package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SnowballItem;
import net.minecraft.util.Hand;

public class QuickThrow extends Module {

    private final ModeSetting item = register(new ModeSetting("Item", "Which throwable item to cycle and throw", "Pearl", "Pearl", "Snowball", "Egg", "All"));
    private final IntSetting cps = register(new IntSetting("CPS", "Throws per second", 4, 1, 10));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public QuickThrow() {
        super("QuickThrow", "Quickly cycles and throws throwable items", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        if (prevSlot >= 0 && mc.player.getInventory().selectedSlot != prevSlot) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        long msPerThrow = 1000L / cps.get();
        if (!timer.hasReached(msPerThrow)) return;

        int slot = findThrowableSlot();
        if (slot == -1) return;

        if (mc.player.getInventory().selectedSlot != slot) {
            prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
        }

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (prevSlot >= 0) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }

        timer.reset();
    }

    private int findThrowableSlot() {
        if (mc.player == null) return -1;
        String mode = item.get();
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            Item it = stack.getItem();
            boolean match = switch (mode) {
                case "Pearl" -> it instanceof EnderPearlItem;
                case "Snowball" -> it instanceof SnowballItem;
                case "Egg" -> it instanceof EggItem;
                case "All" -> it instanceof EnderPearlItem || it instanceof SnowballItem || it instanceof EggItem;
                default -> false;
            };
            if (match) return i;
        }
        return -1;
    }
}
