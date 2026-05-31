package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoBow extends Module {

    private final IntSetting minCharge = register(new IntSetting(
            "Min Charge", "Minimum charge ticks before releasing (20 = full)", 20, 5, 20));

    private final IntSetting range = register(new IntSetting(
            "Range", "Maximum target range in blocks", 30, 5, 60));

    private int chargeTicks = 0;

    public AutoBow() {
        super("AutoBow", "Automatically charges and releases bow at optimal timing", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        chargeTicks = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        ItemStack held = mc.player.getMainHandStack();
        if (!(held.getItem() instanceof BowItem)) {
            chargeTicks = 0;
            return;
        }

        if (findNearestTarget() == null) {
            if (mc.player.isUsingItem()) {
                mc.interactionManager.stopUsingItem(mc.player);
            }
            chargeTicks = 0;
            return;
        }

        if (!mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            chargeTicks = 0;
            return;
        }

        chargeTicks++;

        if (chargeTicks >= minCharge.get()) {
            mc.interactionManager.stopUsingItem(mc.player);
            chargeTicks = 0;
        }
    }

    private PlayerEntity findNearestTarget() {
        PlayerEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity p)) continue;
            if (p.isDead() || p.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(p);
            if (d <= range.get() && d < bestDist) {
                bestDist = d;
                best = p;
            }
        }
        return best;
    }

    private net.minecraft.item.ItemStack mc_held() {
        return mc.player.getMainHandStack();
    }
}
