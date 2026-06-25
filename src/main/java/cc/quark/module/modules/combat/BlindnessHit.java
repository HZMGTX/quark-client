package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class BlindnessHit extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range within which to throw the blindness potion", 5.0, 3.0, 8.0));

    private final TimerUtil timer = new TimerUtil();

    public BlindnessHit() {
        super("BlindnessHit", "Throws a lingering potion of blindness at the target before attacking", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        // Rate-limit to once every 3 seconds to avoid wasting potions
        if (!timer.hasReached(3000)) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        int potionSlot = findPotionSlot();
        if (potionSlot == -1) return;

        int prev = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = potionSlot;

        // Look towards the target before throwing
        double dx = target.getX() - mc.player.getX();
        double dy = (target.getY() + target.getHeight() * 0.5) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = target.getZ() - mc.player.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        mc.player.setYaw((float) Math.toDegrees(Math.atan2(dz, dx)) - 90f);
        mc.player.setPitch((float) -Math.toDegrees(Math.atan2(dy, hDist)));

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prev;
        timer.reset();
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double bestDist = range.get();
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof PlayerEntity le)) continue;
            if (ent == mc.player) continue;
            if (le.isRemoved() || le.getHealth() <= 0f) continue;
            double d = mc.player.distanceTo(le);
            if (d < bestDist) {
                bestDist = d;
                best = le;
            }
        }
        return best;
    }

    private int findPotionSlot() {
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.LINGERING_POTION)) return i;
        }
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.SPLASH_POTION)) return i;
        }
        return -1;
    }
}
