package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AntiPoison2 extends Module {

    private final IntSetting hpToUseGapple = register(new IntSetting(
            "HPForGapple", "Health (half-hearts) at which to use golden apple when withered", 10, 1, 20));

    private final TimerUtil timer = new TimerUtil();

    public AntiPoison2() {
        super("AntiPoison2", "Drinks milk when poisoned; eats golden apple if withered below HP threshold", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(1000)) return;
        timer.reset();

        boolean poisoned = mc.player.hasStatusEffect(StatusEffects.POISON);
        boolean withered = mc.player.hasStatusEffect(StatusEffects.WITHER);
        float health = mc.player.getHealth();

        if (poisoned) {
            useItemFromHotbar(Items.MILK_BUCKET);
        } else if (withered && health <= hpToUseGapple.get()) {
            if (!useItemFromHotbar(Items.GOLDEN_APPLE)) {
                useItemFromHotbar(Items.ENCHANTED_GOLDEN_APPLE);
            }
        }
    }

    private boolean useItemFromHotbar(net.minecraft.item.Item item) {
        if (mc.player == null) return false;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == item) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
                mc.player.getInventory().selectedSlot = prev;
                return true;
            }
        }
        return false;
    }
}
