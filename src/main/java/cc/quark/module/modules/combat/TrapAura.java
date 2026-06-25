package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TrapAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Target range in blocks", 3.5, 1.0, 6.0));
    private final IntSetting    depth = register(new IntSetting("Depth",    "Hole depth (blocks)",    1,   1,   3));

    private long lastActionMs = 0L;

    public TrapAura() {
        super("TrapAura", "Places traps (holes) under enemy feet", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastActionMs = 0L;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastActionMs < 200L) return;

        // Find nearest player
        PlayerEntity target = null;
        double bestDist = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestDist) {
                bestDist = d;
                target   = p;
            }
        }

        if (target == null) return;

        // Attempt to dig blocks under the target
        BlockPos under = target.getBlockPos().down();
        for (int i = 0; i < depth.get(); i++) {
            BlockPos digPos = under.down(i);
            if (mc.world.getBlockState(digPos).isAir()) continue;
            if (mc.world.getBlockState(digPos).getHardness(mc.world, digPos) < 0) continue;

            // Start attacking the block
            Vec3d hitVec = Vec3d.ofCenter(digPos).add(0, 0.5, 0);
            BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, digPos, false);
            mc.interactionManager.attackBlock(digPos, Direction.UP);
            lastActionMs = System.currentTimeMillis();
            break;
        }
    }
}
