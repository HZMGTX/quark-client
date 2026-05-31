package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.passive.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AutoFeed2 extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Block radius to look for tamed animals", 6, 1, 16));

    private final TimerUtil timer = new TimerUtil();

    public AutoFeed2() {
        super("AutoFeed2", "Feeds tamed wolves, cats, and horses with appropriate food", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(2000)) return;
        timer.reset();

        double r = range.get();
        mc.world.getEntitiesByClass(
                TameableEntity.class,
                mc.player.getBoundingBox().expand(r),
                e -> e.isTamed() && e.getOwnerUuid() != null
                        && e.getOwnerUuid().equals(mc.player.getUuid())
                        && e.getHealth() < e.getMaxHealth()
        ).forEach(animal -> {
            Item food = getFoodFor(animal);
            if (food == null) return;

            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == food) {
                    int prev = mc.player.getInventory().selectedSlot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                    mc.player.getInventory().selectedSlot = i;
                    mc.interactionManager.interactEntity(mc.player, animal, Hand.MAIN_HAND);
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
                    mc.player.getInventory().selectedSlot = prev;
                    return;
                }
            }
        });

        mc.world.getEntitiesByClass(
                HorseEntity.class,
                mc.player.getBoundingBox().expand(r),
                e -> e.isTame() && e.getHealth() < e.getMaxHealth()
        ).forEach(horse -> {
            for (int i = 0; i < 9; i++) {
                var stack = mc.player.getInventory().getStack(i);
                if (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.GOLDEN_CARROT) {
                    int prev = mc.player.getInventory().selectedSlot;
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                    mc.player.getInventory().selectedSlot = i;
                    mc.interactionManager.interactEntity(mc.player, horse, Hand.MAIN_HAND);
                    mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
                    mc.player.getInventory().selectedSlot = prev;
                    return;
                }
            }
        });
    }

    private Item getFoodFor(TameableEntity entity) {
        if (entity instanceof WolfEntity) return Items.COOKED_BEEF;
        if (entity instanceof CatEntity)  return Items.COD;
        return null;
    }
}
