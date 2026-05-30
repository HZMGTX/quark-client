package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * FastBridge - helps bridging by automatically placing the block in hand below
 * the player's feet when sneaking near an edge.
 * Delay controls ticks between placements. Godbridge enables scaffold-style
 * bridging without needing to sneak manually.
 */
public class FastBridge extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Ticks between automatic block placements", 1, 1, 5));
    private final BoolSetting godbridge = register(new BoolSetting(
            "Godbridge", "Scaffold-style bridging without sneaking", false));

    private int ticksSincePlaced = 0;

    public FastBridge() {
        super("FastBridge", "Auto-places blocks below feet while bridging", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        ticksSincePlaced = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Require either sneaking or godbridge mode
        boolean shouldPlace = mc.player.isSneaking() || godbridge.isEnabled();
        if (!shouldPlace) return;

        // Check player has a block item in hand
        ItemStack held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof BlockItem)) return;

        // Cooldown between placements
        if (ticksSincePlaced < delay.get()) {
            ticksSincePlaced++;
            return;
        }

        // Attempt to place a block at the position below and slightly behind the player
        BlockPos below = mc.player.getBlockPos().down();

        // Only place if the block below is air
        if (!mc.world.getBlockState(below).isAir()) return;

        // Find a solid block adjacent to place against
        for (Direction side : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP}) {
            BlockPos neighbor = below.offset(side);
            if (!mc.world.getBlockState(neighbor).isAir()) {
                // Place block against this neighbor's face
                Direction placeDir = side.getOpposite();
                Vec3d hitVec = Vec3d.ofCenter(neighbor).add(
                        Vec3d.of(placeDir.getVector()).multiply(0.5));
                BlockHitResult hitResult = new BlockHitResult(hitVec, placeDir, neighbor, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
                mc.player.swingHand(Hand.MAIN_HAND);
                ticksSincePlaced = 0;
                return;
            }
        }
    }
}
