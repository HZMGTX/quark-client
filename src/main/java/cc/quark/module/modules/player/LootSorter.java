package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.component.DataComponentTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LootSorter extends Module {

    private final ModeSetting priority = register(new ModeSetting(
            "Priority", "How to sort dropped loot",
            "Value", "Value", "Rarity", "Weight"));

    private final TimerUtil timer = new TimerUtil();

    public LootSorter() {
        super("LootSorter", "Sorts dropped loot by value automatically", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(100)) return;
        timer.reset();

        List<ItemEntity> lootItems = new ArrayList<>();
        for (var entity : mc.world.getEntities()) {
            if (!(entity instanceof ItemEntity ie)) continue;
            if (mc.player.distanceTo(ie) > 8.0) continue;
            lootItems.add(ie);
        }

        if (lootItems.isEmpty()) return;

        Comparator<ItemEntity> comparator = getComparator();
        lootItems.sort(comparator);

        ItemEntity best = lootItems.get(0);
        Vec3d target = best.getPos();
        Vec3d pos = mc.player.getPos();
        Vec3d dir = target.subtract(pos).normalize().multiply(0.2);

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(
                pos.x + dir.x, pos.y, pos.z + dir.z, mc.player.isOnGround()));
    }

    private Comparator<ItemEntity> getComparator() {
        return switch (priority.get()) {
            case "Rarity" -> Comparator.comparingInt(ie -> -getRarity(ie.getStack()));
            case "Weight" -> Comparator.comparingInt(ie -> -ie.getStack().getCount());
            default       -> Comparator.comparingInt(ie -> -getSimpleValue(ie.getStack()));
        };
    }

    private int getSimpleValue(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        if (id.contains("netherite")) return 100;
        if (id.contains("diamond"))  return 80;
        if (id.contains("emerald"))  return 70;
        if (id.contains("gold"))     return 50;
        if (id.contains("iron"))     return 30;
        if (id.contains("totem"))    return 90;
        return 10;
    }

    private int getRarity(ItemStack stack) {
        return switch (stack.getRarity()) {
            case COMMON    -> 0;
            case UNCOMMON  -> 1;
            case RARE      -> 2;
            case EPIC      -> 3;
        };
    }
}
