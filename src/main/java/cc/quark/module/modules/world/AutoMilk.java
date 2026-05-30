package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoMilk extends Module {

    private final IntSetting range = register(new IntSetting("Range", "Radius to search for cows", 5, 1, 10));
    private final IntSetting delay = register(new IntSetting("Delay", "Cooldown between milking (ms)", 1000, 200, 5000));

    private final TimerUtil timer = new TimerUtil();

    public AutoMilk() {
        super("AutoMilk", "Automatically milks nearby cows when holding a bucket", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Must be holding a bucket in main hand
        if (!mc.player.getMainHandStack().isOf(Items.BUCKET)) return;

        int r = range.get();
        double rangeSq = r * r;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof CowEntity cow)) continue;
            if (mc.player.squaredDistanceTo(cow) > rangeSq) continue;

            mc.interactionManager.interactEntity(mc.player, cow, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            return;
        }
    }
}
