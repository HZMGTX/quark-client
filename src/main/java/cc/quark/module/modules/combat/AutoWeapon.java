package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;

public class AutoWeapon extends Module {

    private final BoolSetting   preferSword = register(new BoolSetting("PreferSword", "Prefer swords over axes",    false));
    private final BoolSetting   preferAxe   = register(new BoolSetting("PreferAxe",   "Prefer axes over swords",    true));
    private final DoubleSetting range       = register(new DoubleSetting("Range",     "Target detection range",     5.0,  1.0, 10.0));

    public AutoWeapon() {
        super("AutoWeapon", "Automatically selects best weapon for target", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // Check if any target is in range
        boolean hasTarget = false;
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (!(e instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(e) <= range.get()) {
                hasTarget = true;
                break;
            }
        }

        if (!hasTarget) return;

        // Find best weapon slot
        int bestSlot = -1;
        double bestDps = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;

            double dps = getWeaponDps(stack);
            if (dps <= 0) continue;

            // Apply preference bonuses
            if (preferSword.isEnabled() && stack.getItem() instanceof SwordItem) dps += 0.5;
            if (preferAxe.isEnabled()   && stack.getItem() instanceof AxeItem)   dps += 0.5;

            if (dps > bestDps) {
                bestDps  = dps;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0 && mc.player.getInventory().selectedSlot != bestSlot) {
            mc.player.getInventory().selectedSlot = bestSlot;
        }
    }

    private double getWeaponDps(ItemStack stack) {
        Item item = stack.getItem();
        // Approximate damage values for vanilla weapons
        if (item instanceof SwordItem sword) {
            // SwordItem base damage depends on material
            return 7.0; // approximate for netherite
        }
        if (item instanceof AxeItem axe) {
            return 9.0; // axes deal more per hit
        }
        if (item instanceof TridentItem) {
            return 9.0;
        }
        return 0.0;
    }

    @Override
    public String getSuffix() {
        if (mc.player == null) return "";
        ItemStack held = mc.player.getMainHandStack();
        if (held.isEmpty()) return "None";
        return held.getItem().getClass().getSimpleName().replace("Item", "");
    }
}
