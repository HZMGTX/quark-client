package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AutoSaddle extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Range to search for rideable mobs", 6, 1, 16));

    private final TimerUtil timer = new TimerUtil();

    public AutoSaddle() {
        super("AutoSaddle", "Automatically saddles tamed horses and pigs in range", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        int saddleSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.SADDLE) {
                saddleSlot = i;
                break;
            }
        }
        if (saddleSlot == -1) return;

        double r = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (mc.player.distanceTo(entity) > r) continue;
            boolean unsaddled = false;
            if (entity instanceof AbstractHorseEntity horse) {
                unsaddled = horse.isTame() && !horse.isSaddled();
            } else if (entity instanceof PigEntity pig) {
                unsaddled = !pig.isSaddled();
            }
            if (!unsaddled) continue;

            int saved = mc.player.getInventory().selectedSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saddleSlot));
            mc.player.getInventory().selectedSlot = saddleSlot;
            mc.interactionManager.interactEntity(mc.player, entity, Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(saved));
            mc.player.getInventory().selectedSlot = saved;
            return;
        }
    }
}
