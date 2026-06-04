package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.item.ShieldItem;
import net.minecraft.util.Hand;

public class ShieldMode extends Module {

    private final DoubleSetting hpTrigger = register(new DoubleSetting(
            "HP Trigger", "Health threshold (hearts) below which auto-blocking activates", 10.0, 1.0, 20.0));

    private final BoolSetting autoBlock = register(new BoolSetting(
            "Auto Block", "Automatically raise shield when health drops below threshold", true));

    private boolean wasBlocking = false;

    public ShieldMode() {
        super("ShieldMode", "Manages shield usage intelligently", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (wasBlocking && mc.options != null) {
            mc.options.useKey.setPressed(false);
            wasBlocking = false;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!autoBlock.isEnabled()) return;

        boolean hasShield = mc.player.getOffHandStack().getItem() instanceof ShieldItem
                || mc.player.getMainHandStack().getItem() instanceof ShieldItem;
        if (!hasShield) return;

        float health = mc.player.getHealth();
        boolean shouldBlock = health <= (float) hpTrigger.get();

        if (shouldBlock && !mc.player.isUsingItem()) {
            // Find shield and use it
            if (mc.player.getOffHandStack().getItem() instanceof ShieldItem) {
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
                mc.options.useKey.setPressed(true);
                wasBlocking = true;
            } else if (mc.player.getMainHandStack().getItem() instanceof ShieldItem) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.options.useKey.setPressed(true);
                wasBlocking = true;
            }
        } else if (!shouldBlock && wasBlocking) {
            mc.options.useKey.setPressed(false);
            wasBlocking = false;
        }
    }
}
