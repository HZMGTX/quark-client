package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * AutoXBow — enhanced auto crossbow that loads, aims, and fires at the closest target.
 * Handles the full load/fire cycle and optionally switches to crossbow automatically.
 */
public class AutoXBow extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 14.0, 2.0, 30.0));
    private final IntSetting loadTicks = register(new IntSetting(
            "Load Ticks", "Ticks to hold use key for loading", 26, 20, 40));
    private final BoolSetting targetPlayers = register(new BoolSetting(
            "Players", "Target players", true));
    private final BoolSetting targetHostiles = register(new BoolSetting(
            "Hostiles", "Target hostile mobs", true));
    private final BoolSetting autoSwitch = register(new BoolSetting(
            "Auto Switch", "Switch hotbar slot to crossbow", true));

    // State machine: 0=idle, 1=loading, 2=loaded/ready to fire
    private int state = 0;
    private int loadCounter = 0;

    public AutoXBow() {
        super("AutoXBow", "Automatically loads and fires crossbow at targets", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        state = 0;
        loadCounter = 0;
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        if (mc.options != null) mc.options.useKey.setPressed(false);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Try to get a crossbow in hand, auto-switch if needed
        if (!(mc.player.getMainHandStack().getItem() instanceof CrossbowItem)) {
            if (!autoSwitch.isEnabled()) return;
            int slot = findCrossbowSlot();
            if (slot == -1) return;
            mc.player.getInventory().selectedSlot = slot;
            return;
        }

        ItemStack held = mc.player.getMainHandStack();
        boolean charged = CrossbowItem.isCharged(held);

        if (charged) {
            // Find a target and fire
            LivingEntity target = findTarget();
            if (target != null) {
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                state = 0;
                loadCounter = 0;
            }
        } else {
            // Need to load
            if (state != 1) {
                mc.options.useKey.setPressed(true);
                state = 1;
                loadCounter = 0;
            } else {
                loadCounter++;
                if (loadCounter >= loadTicks.get()) {
                    mc.options.useKey.setPressed(false);
                    state = 0;
                    loadCounter = 0;
                }
            }
        }
    }

    private int findCrossbowSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof CrossbowItem) return i;
        }
        return -1;
    }

    private LivingEntity findTarget() {
        double r = range.get();
        LivingEntity best = null;
        double bestDist = r;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof LivingEntity le)) continue;
            if (le == mc.player) continue;
            if (le.isRemoved() || le.getHealth() <= 0f) continue;
            if (le instanceof PlayerEntity && !targetPlayers.isEnabled()) continue;
            if (le instanceof HostileEntity && !targetHostiles.isEnabled()) continue;
            if (!(le instanceof PlayerEntity) && !(le instanceof HostileEntity)) continue;
            double d = mc.player.distanceTo(le);
            if (d < bestDist) {
                bestDist = d;
                best = le;
            }
        }
        return best;
    }
}
