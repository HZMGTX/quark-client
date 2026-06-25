package cc.quark.module.modules.world;

import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public class LiquidPlacer extends Module {
    private final ModeSetting liquid = register(new ModeSetting("Liquid", "Which liquid to place", "Water", "Water", "Lava"));
    private final BoolSetting onEnable = register(new BoolSetting("On Enable", "Place when toggled", true));

    public LiquidPlacer() {
        super("Liquid Placer", "Place water or lava at crosshair", Category.WORLD, 0);
    }

    @Override
    public void onEnable() {
        if (!onEnable.isEnabled()) return;
        if (mc.player == null || mc.crosshairTarget == null) { disable(); return; }
        if (!(mc.crosshairTarget instanceof BlockHitResult bhr)) { disable(); return; }

        var target = liquid.get().equals("Water") ? Items.WATER_BUCKET : Items.LAVA_BUCKET;
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(target)) { slot = i; break; }
        }
        if (slot == -1) { ChatUtil.warn("[LiquidPlacer] No " + liquid.get() + " bucket found."); disable(); return; }

        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        disable();
    }
}
