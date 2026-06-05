package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.StringSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * BlockReplace - Replaces one block type with another as you walk.
 * Scans a configurable radius around the player each tick, breaks the
 * source block, and immediately places the replacement block in its spot.
 */
public class BlockReplace extends Module {

    private final StringSetting from = register(new StringSetting(
            "From", "Block to replace (e.g. dirt)", "dirt"));
    private final StringSetting to = register(new StringSetting(
            "To", "Block to place instead (e.g. grass_block)", "grass_block"));
    private final IntSetting radius = register(new IntSetting(
            "Radius", "Search radius around player", 4, 1, 8));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between replacements", 150, 50, 1000));
    private final BoolSetting onlyExposed = register(new BoolSetting(
            "OnlyExposed", "Only replace blocks exposed to air on at least one face", true));
    private final BoolSetting hotbarOnly = register(new BoolSetting(
            "HotbarOnly", "Search only the hotbar for the replacement block", false));

    private final TimerUtil timer = new TimerUtil();

    public BlockReplace() {
        super("BlockReplace", "Replaces one block type with another as you walk", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        Block fromBlock = resolveBlock(from.get());
        Block toBlock   = resolveBlock(to.get());
        if (fromBlock == null || toBlock == null) return;

        int replaceSlot = findReplaceSlot(toBlock);
        if (replaceSlot < 0) return;

        BlockPos center = mc.player.getBlockPos();
        int r = radius.get();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (!mc.world.getBlockState(pos).isOf(fromBlock)) continue;
            if (onlyExposed.isEnabled() && !hasAirNeighbor(pos)) continue;

            BlockPos immutable = pos.toImmutable();

            // Break the source block
            mc.interactionManager.attackBlock(immutable, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);

            // Place replacement on the block below (solid neighbour)
            Direction placeDir = findSolidNeighbour(immutable);
            if (placeDir != null) {
                BlockPos neighbour = immutable.offset(placeDir.getOpposite());
                int prevSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = replaceSlot;
                Vec3d hitVec = Vec3d.ofCenter(neighbour).add(
                        placeDir.getOffsetX() * 0.5,
                        placeDir.getOffsetY() * 0.5,
                        placeDir.getOffsetZ() * 0.5);
                BlockHitResult hit = new BlockHitResult(hitVec, placeDir, neighbour.toImmutable(), false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.getInventory().selectedSlot = prevSlot;
            }

            timer.reset();
            return;
        }
    }

    private boolean hasAirNeighbor(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (mc.world.getBlockState(pos.offset(dir)).isAir()) return true;
        }
        return false;
    }

    private Direction findSolidNeighbour(BlockPos pos) {
        // Prefer the block below for floor placements
        Direction[] preferred = { Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };
        for (Direction dir : preferred) {
            BlockPos neighbour = pos.offset(dir);
            if (mc.world.getBlockState(neighbour).isSolidBlock(mc.world, neighbour)) {
                return dir.getOpposite();
            }
        }
        return null;
    }

    private int findReplaceSlot(Block block) {
        if (mc.player == null) return -1;
        net.minecraft.item.Item item = block.asItem();
        int maxSlot = hotbarOnly.isEnabled() ? 9 : 36;
        for (int i = 0; i < maxSlot; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i < 9 ? i : -1;
        }
        // For hotbar, remap: re-check just hotbar
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) return i;
        }
        return -1;
    }

    private Block resolveBlock(String name) {
        try {
            String id = name.trim().toLowerCase();
            if (!id.contains(":")) id = "minecraft:" + id;
            return Registries.BLOCK.get(Identifier.of(id));
        } catch (Exception e) {
            return null;
        }
    }
}
