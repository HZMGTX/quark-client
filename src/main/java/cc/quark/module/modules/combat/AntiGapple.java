package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class AntiGapple extends Module {

    private final TimerUtil timer = new TimerUtil();

    public AntiGapple() {
        super("AntiGapple", "Interrupts enemy golden apple eating by attacking them", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(300)) return;

        List<PlayerEntity> players = EntityUtil.getEntitiesOfType(PlayerEntity.class, 5.0);
        players.removeIf(p -> p == mc.player || EntityUtil.isFriend(p));
        players.sort(Comparator.comparingDouble(EntityUtil::distanceTo));

        for (PlayerEntity player : players) {
            var mainHand = player.getMainHandStack();
            var offHand = player.getOffHandStack();
            boolean eatingGapple = (mainHand.getItem() == Items.GOLDEN_APPLE || mainHand.getItem() == Items.ENCHANTED_GOLDEN_APPLE
                    || offHand.getItem() == Items.GOLDEN_APPLE || offHand.getItem() == Items.ENCHANTED_GOLDEN_APPLE)
                    && player.isUsingItem();

            if (eatingGapple) {
                mc.interactionManager.attackEntity(mc.player, player);
                mc.player.swingHand(Hand.MAIN_HAND);
                timer.reset();
                return;
            }
        }
    }
}
