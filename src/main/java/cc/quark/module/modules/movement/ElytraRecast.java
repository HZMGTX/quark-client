package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class ElytraRecast extends Module {
    private final DoubleSetting minHeight = register(new DoubleSetting("Min Height", "Minimum height to recast", 5.0, 1.0, 20.0));
    private final BoolSetting autoFire = register(new BoolSetting("Auto Fire", "Auto use firework to boost", false));

    public ElytraRecast() { super("ElytraRecast", "Auto-recasts elytra to keep flying", Category.MOVEMENT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null) return;
        if (!mc.player.isFallFlying()) {
            if (mc.player.getY() > minHeight.get() && !mc.player.isOnGround()) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            }
        } else if (autoFire.isEnabled()) {
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == Items.FIREWORK_ROCKET) {
                    if (mc.player.getVelocity().length() < 0.5) {
                        int prev = mc.player.getInventory().selectedSlot;
                        mc.player.getInventory().selectedSlot = i;
                        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                        mc.player.getInventory().selectedSlot = prev;
                    }
                    break;
                }
            }
        }
    }
}
