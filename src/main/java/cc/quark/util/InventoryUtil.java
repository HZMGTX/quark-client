package cc.quark.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.*;
//? if mc >= "1.20.5" {
import net.minecraft.component.type.FoodComponent;
//?}
import net.minecraft.screen.slot.SlotActionType;

public class InventoryUtil {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static int findItem(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == item) return i;
        }
        return -1;
    }

    public static int findBestSword() {
        if (mc.player == null) return -1;
        int best = -1;
        float bestDmg = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof SwordItem sword) {
                float dmg = sword.getMaterial().attackDamageBonus();
                if (dmg > bestDmg) { bestDmg = dmg; best = i; }
            }
        }
        return best;
    }

    public static int findBestPickaxe() {
        if (mc.player == null) return -1;
        int best = -1;
        float bestSpeed = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof PickaxeItem pick) {
                float speed = pick.getMaterial().speed();
                if (speed > bestSpeed) { bestSpeed = speed; best = i; }
            }
        }
        return best;
    }

    public static int findBestAxe() {
        if (mc.player == null) return -1;
        int best = -1;
        float bestDmg = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof AxeItem axe) {
                float dmg = axe.getMaterial().attackDamageBonus();
                if (dmg > bestDmg) { bestDmg = dmg; best = i; }
            }
        }
        return best;
    }

    public static int countItem(Item item) {
        if (mc.player == null) return 0;
        int count = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == item) count += stack.getCount();
        }
        return count;
    }

    public static boolean hasItem(Item item) {
        return findItem(item) != -1;
    }

    public static int getFreeSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) return i;
        }
        return -1;
    }

    public static void moveToHotbar(int slot, int hotbarSlot) {
        if (mc.player == null || mc.interactionManager == null) return;
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            slot, hotbarSlot, SlotActionType.SWAP, mc.player
        );
    }

    public static void throwItem(int slot) {
        if (mc.player == null || mc.interactionManager == null) return;
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            slot, 1, SlotActionType.THROW, mc.player
        );
    }

    public static void shiftClick(int slot) {
        if (mc.player == null || mc.interactionManager == null) return;
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            slot, 0, SlotActionType.QUICK_MOVE, mc.player
        );
    }

    public static ItemStack getBestFood() {
        if (mc.player == null) return ItemStack.EMPTY;
        ItemStack best = ItemStack.EMPTY;
        int bestFood = 0;
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            //? if mc >= "1.20.5" {
            if (stack.contains(net.minecraft.component.DataComponentTypes.FOOD)) {
                net.minecraft.component.type.FoodComponent fc = stack.get(net.minecraft.component.DataComponentTypes.FOOD);
                if (fc != null) {
                    int hunger = fc.nutrition();
                    if (hunger > bestFood) { bestFood = hunger; best = stack; }
                }
            }
            //?} else {
            /*if (stack.getItem().isFood()) {
                net.minecraft.item.FoodComponent fc = stack.getItem().getFoodComponent();
                if (fc != null) {
                    int hunger = fc.getHunger();
                    if (hunger > bestFood) { bestFood = hunger; best = stack; }
                }
            }*/
            //?}
        }
        return best;
    }
}
