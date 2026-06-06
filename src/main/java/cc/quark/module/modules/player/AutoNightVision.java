package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class AutoNightVision extends Module {

    private final IntSetting  threshold  = register(new IntSetting("Threshold", "Seconds remaining before auto-reapplying", 10, 1, 60));
    private final BoolSetting usePotion  = register(new BoolSetting("Use Potion", "Drink a night vision potion from hotbar", true));
    private final BoolSetting useSplash  = register(new BoolSetting("Allow Splash", "Also use splash/lingering potions", true));
    private final BoolSetting fallback   = register(new BoolSetting("Gamma Fallback", "Use gamma boost if no potion found", true));

    public AutoNightVision() {
        super("AutoNightVision", "Auto-applies night vision potion when the effect is about to expire", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
        if (fallback.isEnabled() && mc.options != null) {
            mc.options.getGamma().setValue(1.0);
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        StatusEffectInstance nvEffect = mc.player.getStatusEffect(StatusEffects.NIGHT_VISION);
        boolean needsNV = nvEffect == null || nvEffect.getDuration() < (threshold.get() * 20);

        if (!needsNV) return;

        if (usePotion.isEnabled()) {
            int slot = findNightVisionPotion();
            if (slot != -1) {
                mc.player.getInventory().selectedSlot = slot;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                return;
            }
        }

        if (fallback.isEnabled()) {
            mc.options.getGamma().setValue(16.0);
        }
    }

    /** Returns hotbar slot (0-8) containing a night vision potion, or -1 if none found. */
    private int findNightVisionPotion() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            boolean isPotion = stack.getItem() == Items.POTION;
            boolean isSplash = useSplash.isEnabled() && (
                    stack.getItem() == Items.SPLASH_POTION ||
                    stack.getItem() == Items.LINGERING_POTION);

            if (!isPotion && !isSplash) continue;

            // Check if it has night vision effect
            var effects = net.minecraft.item.consume.UseAction.EAT == stack.getItem().getUseAction(stack) ? null
                    : stack.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);
            if (effects == null) continue;

            for (StatusEffectInstance e : effects.getEffects()) {
                if (e.getEffectType().equals(StatusEffects.NIGHT_VISION)) return i;
            }
        }
        return -1;
    }
}
