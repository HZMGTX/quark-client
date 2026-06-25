package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * AutoFarm2 - Comprehensive auto-farming assistant.
 * Detects mature crops nearby, breaks them, and optionally replants them.
 */
public class AutoFarm2 extends Module {

    private final IntSetting radius = register(new IntSetting(
            "Radius", "Block radius to search for mature crops", 4, 1, 8));

    private final BoolSetting autoReplant = register(new BoolSetting(
            "Auto Replant", "Replant crops after harvesting", true));

    private final BoolSetting wheat = register(new BoolSetting(
            "Wheat", "Harvest mature wheat", true));

    private final BoolSetting carrots = register(new BoolSetting(
            "Carrots", "Harvest mature carrots", true));

    private final BoolSetting potatoes = register(new BoolSetting(
            "Potatoes", "Harvest mature potatoes", true));

    private final BoolSetting beetroot = register(new BoolSetting(
            "Beetroot", "Harvest mature beetroot", true));

    private final BoolSetting netherwart = register(new BoolSetting(
            "Nether Wart", "Harvest mature nether wart", false));

    private final BoolSetting melonPumpkin = register(new BoolSetting(
            "Melon/Pumpkin", "Break melon and pumpkin blocks", true));

    private final IntSetting delayTicks = register(new IntSetting(
            "Delay", "Ticks between harvest actions", 4, 1, 20));

    private int tickTimer = 0;

    public AutoFarm2() {
        super("AutoFarm2", "Automatically harvests and optionally replants mature crops nearby", Category.MISC);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        tickTimer++;
        if (tickTimer < delayTicks.get()) return;
        tickTimer = 0;

        ClientPlayerEntity player = mc.player;
        World world = mc.world;
        BlockPos origin = player.getBlockPos();
        int r = radius.get();

        for (int x = -r; x <= r; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -r; z <= r; z++) {
                    BlockPos pos = origin.add(x, y, z);
                    BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();

                    if (isMatureCrop(state, block)) {
                        // Break the crop
                        mc.interactionManager.attackBlock(pos, net.minecraft.util.math.Direction.UP);
                        player.swingHand(Hand.MAIN_HAND);

                        // Replant if enabled
                        if (autoReplant.isEnabled()) {
                            tryReplant(pos, block);
                        }
                        return; // one action per interval
                    }
                }
            }
        }
    }

    private boolean isMatureCrop(BlockState state, Block block) {
        if (wheat.isEnabled() && block instanceof CropBlock cb
                && block == Blocks.WHEAT && cb.isMature(state)) return true;

        if (carrots.isEnabled() && block instanceof CropBlock cb
                && block == Blocks.CARROTS && cb.isMature(state)) return true;

        if (potatoes.isEnabled() && block instanceof CropBlock cb
                && block == Blocks.POTATOES && cb.isMature(state)) return true;

        if (beetroot.isEnabled() && block instanceof BeetrootsBlock bb
                && state.get(BeetrootsBlock.CROPS) >= 3) return true;

        if (netherwart.isEnabled() && block == Blocks.NETHER_WART
                && state.get(NetherWartBlock.AGE) == 3) return true;

        if (melonPumpkin.isEnabled()
                && (block == Blocks.MELON || block == Blocks.PUMPKIN)) return true;

        return false;
    }

    private void tryReplant(BlockPos pos, Block harvestedBlock) {
        Item seed = getSeedFor(harvestedBlock);
        if (seed == null) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == seed) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                        new net.minecraft.util.hit.BlockHitResult(
                                pos.toCenterPos(),
                                net.minecraft.util.math.Direction.UP,
                                pos, false));
                mc.player.getInventory().selectedSlot = prev;
                return;
            }
        }
    }

    private Item getSeedFor(Block block) {
        if (block == Blocks.WHEAT)      return Items.WHEAT_SEEDS;
        if (block == Blocks.CARROTS)    return Items.CARROT;
        if (block == Blocks.POTATOES)   return Items.POTATO;
        if (block == Blocks.BEETROOTS)  return Items.BEETROOT_SEEDS;
        if (block == Blocks.NETHER_WART) return Items.NETHER_WART;
        return null;
    }
}
