package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventPacketSend;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.*;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * NoInteract2 - Prevents the player from interacting with configurable categories
 * of blocks or entities by cancelling the relevant outgoing packets.
 *
 * Complements AntiInteract with finer-grained block-type filtering so that,
 * for example, you can suppress chest/crafting GUIs without blocking doors.
 */
public class NoInteract2 extends Module {

    private final BoolSetting noChests = register(new BoolSetting(
            "Chests", "Block interaction with chest-type blocks", true));
    private final BoolSetting noCrafting = register(new BoolSetting(
            "Crafting", "Block interaction with crafting/workbench", false));
    private final BoolSetting noFurnace = register(new BoolSetting(
            "Furnace", "Block interaction with furnace-type blocks", false));
    private final BoolSetting noDoors = register(new BoolSetting(
            "Doors", "Block interaction with doors and trapdoors", false));
    private final BoolSetting noButtons = register(new BoolSetting(
            "Buttons", "Block interaction with buttons and levers", false));
    private final BoolSetting noAnvil = register(new BoolSetting(
            "Anvil", "Block interaction with anvils", false));
    private final BoolSetting noEnchantTable = register(new BoolSetting(
            "Enchant Table", "Block interaction with enchanting tables", false));
    private final BoolSetting noEntities = register(new BoolSetting(
            "Entities", "Block all entity interactions", false));
    private final BoolSetting noItemUse = register(new BoolSetting(
            "Item Use", "Block right-click item use packets", false));

    public NoInteract2() {
        super("NoInteract2", "Prevents interaction with configurable block/entity types", Category.PLAYER);
    }

    @EventHandler
    public void onPacketSend(EventPacketSend event) {
        if (mc.player == null) return;

        if (noEntities.isEnabled() && event.getPacket() instanceof PlayerInteractEntityPacket) {
            event.cancel();
            return;
        }

        if (noItemUse.isEnabled() && event.getPacket() instanceof PlayerInteractItemC2SPacket) {
            event.cancel();
            return;
        }

        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket pkt) {
            BlockPos pos = pkt.getBlockHitResult().getBlockPos();
            World world = mc.world;
            if (world == null) return;

            Block block = world.getBlockState(pos).getBlock();

            if (noChests.isEnabled() && isChestLike(block)) { event.cancel(); return; }
            if (noCrafting.isEnabled() && isCraftingLike(block)) { event.cancel(); return; }
            if (noFurnace.isEnabled() && isFurnaceLike(block)) { event.cancel(); return; }
            if (noDoors.isEnabled() && isDoorLike(block)) { event.cancel(); return; }
            if (noButtons.isEnabled() && isButtonLike(block)) { event.cancel(); return; }
            if (noAnvil.isEnabled() && isAnvilLike(block)) { event.cancel(); return; }
            if (noEnchantTable.isEnabled() && block instanceof EnchantingTableBlock) { event.cancel(); }
        }
    }

    private boolean isChestLike(Block b) {
        return b instanceof ChestBlock
                || b instanceof TrappedChestBlock
                || b instanceof BarrelBlock
                || b instanceof ShulkerBoxBlock
                || b instanceof EnderChestBlock;
    }

    private boolean isCraftingLike(Block b) {
        return b instanceof CraftingTableBlock;
    }

    private boolean isFurnaceLike(Block b) {
        return b instanceof AbstractFurnaceBlock;
    }

    private boolean isDoorLike(Block b) {
        return b instanceof DoorBlock || b instanceof TrapdoorBlock || b instanceof FenceGateBlock;
    }

    private boolean isButtonLike(Block b) {
        return b instanceof AbstractButtonBlock || b instanceof LeverBlock;
    }

    private boolean isAnvilLike(Block b) {
        return b instanceof AnvilBlock;
    }
}
