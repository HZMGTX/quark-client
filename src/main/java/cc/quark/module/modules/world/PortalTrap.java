package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class PortalTrap extends Module {

    private final IntSetting layers = register(new IntSetting(
            "Layers", "Layers of obsidian to surround the portal with", 1, 1, 3));

    private final TimerUtil timer = new TimerUtil();

    public PortalTrap() {
        super("PortalTrap", "Places obsidian around portals to trap players entering", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(200)) return;
        timer.reset();

        int obsSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.OBSIDIAN) {
                obsSlot = i;
                break;
            }
        }
        if (obsSlot == -1) return;

        BlockPos center = mc.player.getBlockPos();
        int l = layers.get();
        List<BlockPos> portalBlocks = new ArrayList<>();

        for (BlockPos pos : BlockPos.iterate(center.add(-10, -3, -10), center.add(10, 3, 10))) {
            if (mc.world.getBlockState(pos).getBlock() instanceof NetherPortalBlock) {
                portalBlocks.add(pos.toImmutable());
            }
        }

        for (BlockPos portal : portalBlocks) {
            for (Direction dir : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST}) {
                for (int layer = 1; layer <= l; layer++) {
                    BlockPos target = portal.offset(dir, layer);
                    if (mc.world.getBlockState(target).isAir()) {
                        BlockPos support = target.down();
                        if (!mc.world.getBlockState(support).isSolidBlock(mc.world, support)) continue;

                        int saved = mc.player.getInventory().selectedSlot;
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(obsSlot));
                        mc.player.getInventory().selectedSlot = obsSlot;
                        BlockHitResult hit = new BlockHitResult(
                                Vec3d.ofCenter(support).add(0, 0.5, 0), Direction.UP, support, false);
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                        mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saved));
                        mc.player.getInventory().selectedSlot = saved;
                        return;
                    }
                }
            }
        }
    }
}
