package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoPlace extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Placement range (blocks)", 4, 1, 6));
    private final IntSetting delay = register(new IntSetting("Delay", "Ticks between placements", 5, 1, 20));
    private int ticker = 0;

    public AutoPlace() {
        super("AutoPlace", "Automatically places blocks from offhand at crosshair target", Category.WORLD);
    }

    @Override
    public void onEnable() { ticker = 0; }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (++ticker < delay.get()) return;
        ticker = 0;

        var offhand = mc.player.getOffHandStack();
        if (offhand.isEmpty() || !(offhand.getItem() instanceof BlockItem)) return;

        HitResult hit = mc.crosshairTarget;
        if (hit == null || hit.getType() != HitResult.Type.BLOCK) return;

        var bhr = (BlockHitResult) hit;
        if (mc.player.getEyePos().distanceTo(bhr.getPos()) > range.get()) return;

        mc.interactionManager.interactBlock(mc.player, Hand.OFF_HAND, bhr);
        mc.player.swingHand(Hand.OFF_HAND);
    }
}
