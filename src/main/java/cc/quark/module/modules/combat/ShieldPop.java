package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class ShieldPop extends Module {

    private final BoolSetting autoSwitch = register(new BoolSetting(
            "AutoSwitch", "Automatically switch to an axe when enemy uses shield", true));

    private final TimerUtil timer = new TimerUtil();

    public ShieldPop() {
        super("ShieldPop", "Rapidly breaks enemy shields using axe cooldown", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(10)) return;

        PlayerEntity target = findShieldingTarget();
        if (target == null) return;

        int axeSlot = findAxeSlot();
        if (axeSlot == -1) return;

        if (autoSwitch.isEnabled()) {
            mc.player.getInventory().selectedSlot = axeSlot;
        } else {
            ItemStack held = mc.player.getMainHandStack();
            if (!(held.getItem() instanceof AxeItem)) return;
        }

        if (mc.player.getAttackCooldownProgress(0f) < 1f) return;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }

    private PlayerEntity findShieldingTarget() {
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            if (!p.isBlocking()) continue;
            double d = mc.player.distanceTo(p);
            if (d <= 4.0 && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private int findAxeSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof AxeItem) return i;
        }
        return -1;
    }
}
