package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class IceRoad extends Module {
    private final IntSetting delay = register(new IntSetting("Delay","ms between placements",200,50,1000));
    private final TimerUtil timer  = new TimerUtil();
    public IceRoad() { super("IceRoad","Places packed ice in front of the player automatically",Category.WORLD); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null||mc.world==null||mc.interactionManager==null) return;
        if (!timer.hasReached(delay.get())) return;
        int slot = -1;
        for (int i=0;i<9;i++) {
            var s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.PACKED_ICE)||s.isOf(Items.BLUE_ICE)||s.isOf(Items.ICE)) { slot=i; break; }
        }
        if (slot<0) return;
        float yaw = (float)Math.toRadians(mc.player.getYaw());
        BlockPos front = mc.player.getBlockPos().add((int)Math.round(-Math.sin(yaw)),0,(int)Math.round(Math.cos(yaw)));
        BlockPos below = front.down();
        if (!mc.world.getBlockState(below).isAir()) {
            int prev = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = slot;
            var hit = new BlockHitResult(Vec3d.ofCenter(below).add(0,0.5,0), Direction.UP, below, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prev;
            timer.reset();
        }
    }
}
