package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class KillOnLow extends Module {

    private final DoubleSetting hpThreshold = register(new DoubleSetting(
            "HpThreshold", "Target players at or below this HP (hearts)", 4.0, 0.5, 20.0));

    private final BoolSetting notify = register(new BoolSetting(
            "Notify", "Show a chat message when a low-HP target is found", true));

    public KillOnLow() {
        super("KillOnLow", "Targets players who are low on health", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        float threshold = (float) hpThreshold.get();

        java.util.List<net.minecraft.client.network.AbstractClientPlayerEntity> lowHpPlayers = mc.world.getPlayers().stream()
                .filter(p -> p != mc.player)
                .filter(p -> !p.isRemoved())
                .filter(p -> p.getHealth() <= threshold)
                .sorted(Comparator.comparingDouble(p -> p.squaredDistanceTo(mc.player)))
                .toList();

        if (lowHpPlayers.isEmpty()) return;

        PlayerEntity target = lowHpPlayers.get(0);

        if (notify.isEnabled()) {
            ChatUtil.info("[KillOnLow] Targeting " + target.getName().getString()
                    + " (" + String.format("%.1f", target.getHealth() / 2f) + " hearts)");
        }

        // Attack the closest low-HP player
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
