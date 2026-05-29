package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class TorchPlacer extends Module {

    private final IntSetting lightThreshold = register(new IntSetting(
            "Light Threshold", "Place torch when light level is below this value", 7, 0, 15));
    private final BoolSetting onlyWhenMining = register(new BoolSetting(
            "Only When Mining", "Only place torches while the attack key is held", false));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between torch placements", 500, 100, 3000));

    private final TimerUtil timer = new TimerUtil();

    public TorchPlacer() {
        super("TorchPlacer", "Automatically places torches in dark areas while mining", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;
        if (onlyWhenMining.isEnabled() && !mc.options.attackKey.isPressed()) return;

        BlockPos feet = mc.player.getBlockPos();
        int lightLevel = mc.world.getLightLevel(feet);
        if (lightLevel >= lightThreshold.get()) return;

        // find torch in hotbar
        int torchSlot = -1;
        for (int i = 0; i < 9; i++) {
            var item = mc.player.getInventory().getStack(i).getItem();
            if (item == Items.TORCH || item == Items.WALL_TORCH) {
                torchSlot = i;
                break;
            }
        }
        if (torchSlot == -1) return;

        // need a solid floor to place on
        BlockPos below = feet.down();
        if (mc.world.getBlockState(below).isAir()) return;
        if (!mc.world.getBlockState(feet).isAir()) return;

        int savedSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = torchSlot;

        Vec3d hitVec = Vec3d.ofCenter(below).add(0, 0.5, 0);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, below, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = savedSlot;
        timer.reset();
    }
}
