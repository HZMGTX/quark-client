package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoDoor extends Module {

    private final BoolSetting doors     = register(new BoolSetting("Doors",      "Auto-open/close doors",      true));
    private final BoolSetting gates     = register(new BoolSetting("Fence Gates","Auto-open/close fence gates",true));
    private final BoolSetting trapdoors = register(new BoolSetting("Trapdoors",  "Auto-open trapdoors",        false));

    public AutoDoor() {
        super("AutoDoor", "Automatically opens and closes doors and fence gates as you approach", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-2, -1, -2), center.add(2, 2, 2))) {
            var state = mc.world.getBlockState(pos);
            var block = state.getBlock();

            boolean isDoor      = doors.isEnabled()     && block instanceof DoorBlock;
            boolean isGate      = gates.isEnabled()      && block instanceof FenceGateBlock;
            boolean isTrapdoor  = trapdoors.isEnabled()  && block instanceof TrapdoorBlock;

            if (!isDoor && !isGate && !isTrapdoor) continue;

            boolean open = state.contains(Properties.OPEN) && state.get(Properties.OPEN);
            double dist  = mc.player.squaredDistanceTo(Vec3d.ofCenter(pos));

            if (!open && dist < 4.0) {
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            } else if (open && dist > 9.0 && dist < 16.0) {
                BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            }
        }
    }
}
