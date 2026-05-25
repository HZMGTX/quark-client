package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AntiDebuff extends Module {

    private final BoolSetting antiPoison    = register(new BoolSetting("Poison",    "Drink milk when poisoned",  true));
    private final BoolSetting antiWeakness  = register(new BoolSetting("Weakness",  "Drink milk when weakened",  true));
    private final BoolSetting antiSlowness  = register(new BoolSetting("Slowness",  "Drink milk when slowed",    false));
    private final BoolSetting antiWither    = register(new BoolSetting("Wither",    "Drink milk when withered",  true));

    private int cooldown = 0;

    public AntiDebuff() {
        super("AntiDebuff", "Drinks milk automatically to cure harmful potion effects", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.interactionManager == null) return;
        if (cooldown > 0) { cooldown--; return; }

        boolean needsMilk =
                (antiPoison.isEnabled()   && mc.player.hasStatusEffect(StatusEffects.POISON))   ||
                (antiWeakness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) ||
                (antiSlowness.isEnabled() && mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) ||
                (antiWither.isEnabled()   && mc.player.hasStatusEffect(StatusEffects.WITHER));

        if (!needsMilk) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.MILK_BUCKET) {
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                cooldown = 40;
                return;
            }
        }
    }
}
