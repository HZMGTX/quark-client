package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.module.modules.render.NotificationOverlay;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public class ArmorAlert extends Module {

    private final IntSetting durabilityThreshold = register(new IntSetting(
            "Threshold", "Warn when armor durability percentage drops below this", 20, 1, 100));
    private final IntSetting cooldownSec = register(new IntSetting(
            "Cooldown", "Seconds between repeated warnings for the same piece", 30, 5, 120));

    private final TimerUtil[] timers = { new TimerUtil(), new TimerUtil(), new TimerUtil(), new TimerUtil() };
    private static final String[] SLOTS = { "Helmet", "Chestplate", "Leggings", "Boots" };

    public ArmorAlert() {
        super("ArmorAlert", "Warns when any armor piece drops below the configured durability threshold", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null) return;

        ItemStack[] armor = {
            mc.player.getInventory().getArmorStack(3),
            mc.player.getInventory().getArmorStack(2),
            mc.player.getInventory().getArmorStack(1),
            mc.player.getInventory().getArmorStack(0)
        };

        for (int i = 0; i < 4; i++) {
            ItemStack stack = armor[i];
            if (stack.isEmpty() || !(stack.getItem() instanceof ArmorItem)) continue;

            int maxDur = stack.getMaxDamage();
            if (maxDur <= 0) continue;

            int remaining = maxDur - stack.getDamage();
            int percent = (remaining * 100) / maxDur;

            if (percent <= durabilityThreshold.get()
                    && timers[i].hasReached((long) cooldownSec.get() * 1000L)) {
                NotificationOverlay.send("ArmorAlert", SLOTS[i] + " at " + percent + "%!", NotificationOverlay.NotifType.WARNING);
                timers[i].reset();
            }
        }
    }
}
