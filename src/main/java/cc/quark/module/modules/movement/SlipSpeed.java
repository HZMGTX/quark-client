package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.util.math.BlockPos;

public class SlipSpeed extends Module {
    private final DoubleSetting boost = register(new DoubleSetting("Boost", "Speed boost on ice/slime", 1.5, 1.0, 5.0));

    public SlipSpeed() { super("SlipSpeed", "Move faster on ice and slime blocks", Category.MOVEMENT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        BlockPos underPos = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.1, mc.player.getZ());
        var underBlock = mc.world.getBlockState(underPos).getBlock();
        boolean isSlippery = (underBlock == Blocks.ICE || underBlock == Blocks.PACKED_ICE || underBlock == Blocks.BLUE_ICE || underBlock == Blocks.SLIME_BLOCK);
        if (isSlippery && mc.player.isOnGround()) {
            var vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * boost.get(), vel.y, vel.z * boost.get());
        }
    }
}
