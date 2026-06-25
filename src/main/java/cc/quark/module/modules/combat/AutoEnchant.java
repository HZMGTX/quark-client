package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.util.math.BlockPos;

public class AutoEnchant extends Module {

    private final IntSetting level = register(new IntSetting(
            "Level", "Enchant level to select (1-3)", 3, 1, 3));

    private final TimerUtil timer = new TimerUtil();

    public AutoEnchant() {
        super("AutoEnchant", "Auto-applies enchanting table process when standing near one; selects best enchant", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(500)) return;

        boolean nearTable = false;
        BlockPos playerPos = mc.player.getBlockPos();
        for (int dx = -2; dx <= 2 && !nearTable; dx++) {
            for (int dy = -1; dy <= 1 && !nearTable; dy++) {
                for (int dz = -2; dz <= 2 && !nearTable; dz++) {
                    if (mc.world.getBlockState(playerPos.add(dx, dy, dz)).getBlock() == Blocks.ENCHANTING_TABLE) {
                        nearTable = true;
                    }
                }
            }
        }
        if (!nearTable) return;

        if (!(mc.currentScreen instanceof EnchantmentScreen)) return;
        if (!(mc.player.currentScreenHandler instanceof EnchantmentScreenHandler handler)) return;

        int choice = level.get() - 1;
        int[] costs = handler.enchantmentPower;
        if (costs == null) return;

        int chosenSlot = -1;
        for (int i = choice; i >= 0; i--) {
            if (costs[i] > 0) {
                chosenSlot = i;
                break;
            }
        }
        if (chosenSlot == -1) return;

        mc.interactionManager.clickButton(handler.syncId, chosenSlot);
        timer.reset();
    }
}
