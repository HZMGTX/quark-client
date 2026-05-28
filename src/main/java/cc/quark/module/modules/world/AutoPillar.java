package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoPillar extends Module {

    private final DoubleSetting height  = register(new DoubleSetting("Height",    "Maximum pillar height in blocks", 30.0, 1.0, 256.0));
    private final BoolSetting autoStop  = register(new BoolSetting("Auto Stop",   "Disable when max height reached", true));

    private double startY = 0;

    public AutoPillar() {
        super("AutoPillar", "Places blocks beneath you to pillar upward while space is held", Category.WORLD);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) startY = mc.player.getY();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (!mc.options.jumpKey.isPressed()) return;

        double builtHeight = mc.player.getY() - startY;
        if (builtHeight >= height.get()) {
            if (autoStop.isEnabled()) disable();
            return;
        }

        int blockSlot = findBlockSlot();
        if (blockSlot == -1) return;

        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) {
            mc.player.jump();
            return;
        }

        BlockPos support = below.down();
        if (mc.world.getBlockState(support).isAir()) return;

        int saved = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = blockSlot;

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(support).add(0, 0.5, 0), Direction.UP, support.toImmutable(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.jump();

        mc.player.getInventory().selectedSlot = saved;
    }

    private int findBlockSlot() {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            if (!mc.player.getInventory().getStack(i).isEmpty()
                    && mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
}
