package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.Optional;

public class AutoCure extends Module {

    private final IntSetting range = register(new IntSetting(
            "Range", "Block radius to search for zombie villagers", 5, 1, 10));

    private final TimerUtil timer = new TimerUtil();

    public AutoCure() {
        super("AutoCure", "Cures zombie villagers automatically using golden apple + weakness", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(2000)) return;
        timer.reset();

        Optional<ZombieVillagerEntity> target = mc.world.getEntitiesByClass(
                ZombieVillagerEntity.class,
                mc.player.getBoundingBox().expand(range.get()),
                e -> e.isAlive() && !e.hasStatusEffect(StatusEffects.WEAKNESS))
                .stream()
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.player)));

        if (target.isEmpty()) return;
        ZombieVillagerEntity zombie = target.get();

        int weaknessSlot = findItem(Items.SPLASH_POTION);
        if (weaknessSlot != -1) {
            int prev = mc.player.getInventory().selectedSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(weaknessSlot));
            mc.player.getInventory().selectedSlot = weaknessSlot;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
            mc.player.getInventory().selectedSlot = prev;
            return;
        }

        if (zombie.hasStatusEffect(StatusEffects.WEAKNESS)) {
            int appleSlot = findItem(Items.GOLDEN_APPLE);
            if (appleSlot == -1) return;
            int prev = mc.player.getInventory().selectedSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(appleSlot));
            mc.player.getInventory().selectedSlot = appleSlot;
            mc.interactionManager.interactEntity(mc.player, zombie, Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(prev));
            mc.player.getInventory().selectedSlot = prev;
        }
    }

    private int findItem(net.minecraft.item.Item item) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == item) return i;
        }
        return -1;
    }
}
