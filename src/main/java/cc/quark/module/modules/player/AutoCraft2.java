package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class AutoCraft2 extends Module {

    private final ModeSetting item = register(new ModeSetting(
            "Item", "Item to auto-craft at a crafting table",
            "Arrows", "Arrows", "Sticks", "Planks"));

    private boolean crafting = false;

    public AutoCraft2() {
        super("AutoCraft2", "Auto-crafts items using crafting table", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        crafting = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // If a crafting screen is open, try to grab the result
        if (mc.player.currentScreenHandler instanceof CraftingScreenHandler craftingHandler) {
            crafting = true;
            grabCraftingResult(craftingHandler);
            return;
        }

        crafting = false;
        if (mc.currentScreen != null) return;

        // Find nearby crafting table
        BlockPos playerPos = mc.player.getBlockPos();
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos check = playerPos.add(x, y, z);
                    if (mc.world.getBlockState(check).getBlock() != Blocks.CRAFTING_TABLE) continue;

                    Vec3d center = Vec3d.ofCenter(check);
                    if (center.distanceTo(mc.player.getEyePos()) > 4.5) continue;

                    BlockHitResult hit = new BlockHitResult(center, Direction.UP, check, false);
                    mc.interactionManager.interactBlock(mc.player, net.minecraft.util.Hand.MAIN_HAND, hit);
                    return;
                }
            }
        }
    }

    private void grabCraftingResult(CraftingScreenHandler craftingHandler) {
        // Slot 0 is the result slot in crafting screen
        if (!craftingHandler.slots.get(0).getStack().isEmpty()) {
            mc.interactionManager.clickSlot(
                    craftingHandler.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
            ChatUtil.info("[AutoCraft2] Crafted: " + item.get());
        } else {
            // Close and notify if materials ran out
            mc.player.closeHandledScreen();
            crafting = false;
            ChatUtil.warn("[AutoCraft2] No more materials for " + item.get());
        }
    }
}
