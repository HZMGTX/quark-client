package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/**
 * AutoSaddle - automatically saddles nearby tamed horses when you have a saddle
 * in the hotbar and the horse is missing one.
 */
public class AutoSaddle extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to look for animals", 4.0, 1.0, 8.0));

    private final TimerUtil timer = new TimerUtil();

    public AutoSaddle() {
        super("Auto Saddle", "Auto-saddle nearby tamed horses when holding a saddle", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        // Find saddle in hotbar (slots 0-8)
        int saddleSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.SADDLE)) {
                saddleSlot = i;
                break;
            }
        }
        if (saddleSlot == -1) return;

        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof HorseEntity horse)) continue;
            if (!horse.isTame()) continue;
            if (horse.isSaddled()) continue;
            if (entity.distanceTo(mc.player) > range.get()) continue;

            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = saddleSlot;
            mc.interactionManager.interactEntity(mc.player, horse, Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prevSlot;
            timer.reset();
            return;
        }
    }
}
