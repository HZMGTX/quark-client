package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.util.Hand;

public class AutoFish2 extends Module {
    private final IntSetting threshold = register(new IntSetting("Threshold", "Bobber drop threshold to reel in", 3, 1, 10));
    private final BoolSetting recast = register(new BoolSetting("Auto Recast", "Automatically recast after catching", true));
    private final TimerUtil recastTimer = new TimerUtil();
    private float lastY = 0;
    private int dropCount = 0;

    public AutoFish2() { super("AutoFish2", "Improved auto-fishing with better bite detection", Category.MISC); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof FishingRodItem)) return;

        FishingBobberEntity bobber = mc.player.fishHook;
        if (bobber == null) {
            if (recast.isEnabled() && recastTimer.hasReached(500)) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                recastTimer.reset();
            }
            return;
        }

        float currentY = (float) bobber.getY();
        if (currentY < lastY - 0.05f) dropCount++;
        else dropCount = 0;
        lastY = currentY;

        if (dropCount >= threshold.get()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            recastTimer.reset();
            dropCount = 0;
        }
    }
}
