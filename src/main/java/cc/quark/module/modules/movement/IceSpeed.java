package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

public class IceSpeed extends Module {
    private final DoubleSetting multiplier = register(new DoubleSetting("Multiplier","Speed multiplier on ice",2.5,1.0,6.0));
    public IceSpeed() { super("IceSpeed","Increases movement speed when on ice",Category.MOVEMENT); }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player==null||mc.world==null) return;
        BlockPos under = mc.player.getBlockPos().down();
        var block = mc.world.getBlockState(under).getBlock();
        if (block==Blocks.ICE||block==Blocks.PACKED_ICE||block==Blocks.BLUE_ICE) {
            var vel = mc.player.getVelocity();
            mc.player.setVelocity(vel.x * multiplier.get(), vel.y, vel.z * multiplier.get());
        }
    }
}
