package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class NetherRoof extends Module {

    private final BoolSetting autoBuild = register(new BoolSetting(
            "AutoBuild", "Auto-place blocks to build a path on the nether roof", false));

    public NetherRoof() {
        super("NetherRoof", "Assists with nether roof access via portal manipulation", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (mc.world.getRegistryKey() != World.NETHER) return;

        double y = mc.player.getY();
        if (y < 120) return;

        if (autoBuild.isEnabled() && mc.interactionManager != null) {
            BlockPos feet = mc.player.getBlockPos();
            BlockPos below = feet.down();
            if (mc.world.getBlockState(below).isAir()) {
                int blockSlot = findSolidBlockSlot();
                if (blockSlot != -1) {
                    int saved = mc.player.getInventory().selectedSlot;
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(blockSlot));
                    mc.player.getInventory().selectedSlot = blockSlot;
                    BlockPos support = below.down();
                    BlockHitResult hit = new BlockHitResult(
                            Vec3d.ofCenter(support).add(0, 0.5, 0), Direction.UP, support, false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saved));
                    mc.player.getInventory().selectedSlot = saved;
                }
            }
        }

        mc.getNetworkHandler().sendPacket(
                new PlayerMoveC2SPacket.PositionAndOnGround(
                        mc.player.getX(), y, mc.player.getZ(), true));
    }

    private int findSolidBlockSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            var item = stack.getItem();
            if (item == net.minecraft.item.Items.COBBLESTONE
                    || item == net.minecraft.item.Items.NETHERRACK
                    || item == net.minecraft.item.Items.STONE) return i;
        }
        return -1;
    }
}
