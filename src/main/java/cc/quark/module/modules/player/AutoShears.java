package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class AutoShears extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Block radius to search for sheep/mooshroom", 4.0, 1.0, 10.0));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between shearing actions", 500, 100, 3000));

    private final TimerUtil timer = new TimerUtil();

    public AutoShears() {
        super("AutoShears", "Automatically shears sheep and mushroom cows", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Locate shears in hotbar
        int shearSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.SHEARS) {
                shearSlot = i;
                break;
            }
        }
        if (shearSlot == -1) return;

        double r = range.get();
        Box box = mc.player.getBoundingBox().expand(r);

        // Collect shearable entities
        List<net.minecraft.entity.Entity> targets = new ArrayList<>();
        mc.world.getEntitiesByClass(SheepEntity.class, box,
                s -> !s.isSheared() && s.distanceTo(mc.player) <= r).forEach(targets::add);
        mc.world.getEntitiesByClass(MooshroomEntity.class, box,
                m -> m.distanceTo(mc.player) <= r).forEach(targets::add);

        if (targets.isEmpty()) return;

        mc.player.getInventory().selectedSlot = shearSlot;
        mc.interactionManager.interactEntity(mc.player, targets.get(0), Hand.MAIN_HAND);
        timer.reset();
    }
}
