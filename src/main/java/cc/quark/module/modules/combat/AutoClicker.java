package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoClicker extends Module {

    private final DoubleSetting cps       = register(new DoubleSetting("CPS",         "Clicks per second",               12.0, 1.0, 20.0));
    private final BoolSetting   leftClick = register(new BoolSetting("Left Click",    "Auto left-click (attack)",         true));
    private final BoolSetting   rightClick= register(new BoolSetting("Right Click",   "Auto right-click (use item/block)",false));
    private final BoolSetting   onlyHeld  = register(new BoolSetting("Only When Held","Only click while holding attack key",true));

    private long lastClick = 0;

    public AutoClicker() {
        super("AutoClicker", "Automatically clicks at a configurable CPS", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (onlyHeld.isEnabled() && !mc.options.attackKey.isPressed()) return;

        long now = System.currentTimeMillis();
        long delay = (long)(1000.0 / cps.get());
        if (now - lastClick < delay) return;
        lastClick = now;

        if (leftClick.isEnabled() && mc.crosshairTarget != null
                && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult ehr = (EntityHitResult) mc.crosshairTarget;
            mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        if (rightClick.isEnabled()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }
}
