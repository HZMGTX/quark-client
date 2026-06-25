package cc.quark.module.modules.world;

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

public class AutoTNT extends Module {
    private final IntSetting amount = register(new IntSetting("Amount", "TNT blocks to place", 1, 1, 10));
    private final BoolSetting ignite = register(new BoolSetting("Auto Ignite", "Light with flint and steel", true));
    private final TimerUtil timer = new TimerUtil();

    public AutoTNT() {
        super("Auto TNT", "Places and ignites TNT at target block", Category.WORLD, 0);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.crosshairTarget == null) { disable(); return; }
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) { disable(); return; }
        BlockPos target = bhr.getBlockPos().offset(Direction.UP);

        // Find TNT in hotbar
        int tntSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.TNT)) { tntSlot = i; break; }
        }
        if (tntSlot == -1) { disable(); return; }

        mc.player.getInventory().selectedSlot = tntSlot;
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
            new BlockHitResult(Vec3d.ofCenter(target), Direction.UP, target, false));
        disable();
    }
}
