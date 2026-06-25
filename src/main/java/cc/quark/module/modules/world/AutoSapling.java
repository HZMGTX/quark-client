package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoSapling extends Module {
    private final TimerUtil timer = new TimerUtil();
    public AutoSapling() { super("AutoSapling","Plants saplings on bare dirt/grass automatically",Category.WORLD); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null||mc.world==null||mc.interactionManager==null) return;
        if (!timer.hasReached(600)) return;
        int slot=-1;
        for (int i=0;i<9;i++) {
            var s=mc.player.getInventory().getStack(i);
            if (s.isOf(Items.OAK_SAPLING)||s.isOf(Items.BIRCH_SAPLING)||s.isOf(Items.SPRUCE_SAPLING)){slot=i;break;}
        }
        if (slot<0) return;
        int r=4; BlockPos p=mc.player.getBlockPos();
        for (BlockPos pos : BlockPos.iterate(p.add(-r,-1,-r),p.add(r,1,r))) {
            var b=mc.world.getBlockState(pos).getBlock();
            if ((b==Blocks.DIRT||b==Blocks.GRASS_BLOCK)&&mc.world.getBlockState(pos.up()).isAir()
                    &&mc.player.getPos().distanceTo(Vec3d.ofCenter(pos))<r) {
                int prev=mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot=slot;
                var hit=new BlockHitResult(Vec3d.ofCenter(pos).add(0,0.5,0),Direction.UP,pos,false);
                mc.interactionManager.interactBlock(mc.player,Hand.MAIN_HAND,hit);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot=prev;
                timer.reset(); return;
            }
        }
    }
}
