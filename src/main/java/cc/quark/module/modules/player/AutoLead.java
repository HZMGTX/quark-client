package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoLead extends Module {

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between lead attachment attempts", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public AutoLead() {
        super("AutoLead", "Automatically attaches a lead to targeted passive animals", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Check if player is holding a lead
        boolean holdingLead = mc.player.getMainHandStack().getItem() == Items.LEAD
                || mc.player.getOffHandStack().getItem() == Items.LEAD;
        if (!holdingLead) return;

        // Check targeted entity
        if (mc.targetedEntity == null) return;
        Entity target = mc.targetedEntity;
        if (!(target instanceof AnimalEntity)) return;

        Hand hand = mc.player.getMainHandStack().getItem() == Items.LEAD ? Hand.MAIN_HAND : Hand.OFF_HAND;
        mc.interactionManager.interactEntity(mc.player, target, hand);
        timer.reset();
    }
}
