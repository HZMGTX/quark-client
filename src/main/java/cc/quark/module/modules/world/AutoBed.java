package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.BedBlock;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBed extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Search range for beds", 4.0, 1.0, 8.0));

    private int cooldown = 0;

    public AutoBed() {
        super("AutoBed", "Automatically sleeps in nearby beds when the sky is dark enough", Category.WORLD);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (--cooldown > 0) return;

        if (!mc.world.isNight()) return;

        int r = (int) range.get();
        BlockPos center = mc.player.getBlockPos();

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -1, -r), center.add(r, 1, r))) {
            if (!(mc.world.getBlockState(pos).getBlock() instanceof BedBlock)) continue;
            if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > range.get() * range.get()) continue;

            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            cooldown = 40;
            return;
        }
    }
}
