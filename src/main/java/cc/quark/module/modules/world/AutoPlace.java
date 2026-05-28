package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class AutoPlace extends Module {

    private final BoolSetting onlyAir = register(new BoolSetting("Only Air", "Only place when target position is air", true));
    private final IntSetting delayMs  = register(new IntSetting("Delay", "Milliseconds between placements", 100, 0, 1000));

    private final TimerUtil timer = new TimerUtil();

    public AutoPlace() {
        super("AutoPlace", "Automatically places blocks from hotbar when crosshair targets a block surface", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult bhr = (BlockHitResult) hit;
        BlockPos placePos = bhr.getBlockPos().offset(bhr.getSide());

        if (onlyAir.isEnabled() && !mc.world.getBlockState(placePos).isAir()) return;

        int slot = findBlockSlot();
        if (slot == -1) return;

        int saved = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.getInventory().selectedSlot = saved;
        timer.reset();
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
