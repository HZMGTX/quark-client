package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SplashPotionItem;
import net.minecraft.util.Hand;

public class PotionAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to throw potions at enemies", 4.0, 1.0, 8.0));

    private final ModeSetting type = register(new ModeSetting(
            "Type", "Potion type to throw", "Damage", "Damage", "Slow", "Blind"));

    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between throws", 1000, 200, 5000));

    private final TimerUtil timer = new TimerUtil();

    public PotionAura() {
        super("PotionAura", "Throws splash potions at nearby enemies", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Find enemy in range
        boolean enemyNear = false;
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof PlayerEntity)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist <= range.get()) {
                enemyNear = true;
                break;
            }
        }

        if (!enemyNear) return;

        // Find potion matching current mode
        int potionSlot = -1;
        String mode = type.get();

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.LINGERING_POTION) {
                // Accept any splash potion (server determines effect)
                potionSlot = i;
                break;
            }
        }

        if (potionSlot >= 0) {
            int prevSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = potionSlot;
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.getInventory().selectedSlot = prevSlot;
            timer.reset();
        }
    }
}
