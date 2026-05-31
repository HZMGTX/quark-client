package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Hand;

public class TridentBoomerang extends Module {

    private final BoolSetting autoCatch = register(new BoolSetting("AutoCatch", "Auto-switches to trident slot when it returns", true));

    private final TimerUtil throwTimer = new TimerUtil();
    private boolean thrown = false;
    private int tridentSlotCache = -1;

    public TridentBoomerang() {
        super("TridentBoomerang", "Throws loyalty trident at target and waits for return", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        thrown = false;
        tridentSlotCache = -1;
        throwTimer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (thrown) {
            boolean tridentInFlight = false;
            for (Entity e : mc.world.getEntities()) {
                if (e instanceof TridentEntity t && t.getOwner() == mc.player) {
                    tridentInFlight = true;
                    break;
                }
            }
            if (!tridentInFlight) {
                thrown = false;
                if (autoCatch.isEnabled() && tridentSlotCache != -1) {
                    mc.player.getInventory().selectedSlot = tridentSlotCache;
                }
            }
            return;
        }

        if (!throwTimer.hasReached(1500)) return;

        int tridentSlot = findLoyaltyTrident();
        if (tridentSlot == -1) return;

        PlayerEntity target = findNearestEnemy(16);
        if (target == null) return;

        tridentSlotCache = tridentSlot;
        mc.player.getInventory().selectedSlot = tridentSlot;
        mc.options.useKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        mc.options.useKey.setPressed(false);
        thrown = false;
    }

    private int findLoyaltyTrident() {
        if (mc.world == null) return -1;
        var loyaltyKey = mc.world.getRegistryManager()
                .getOptional(RegistryKeys.ENCHANTMENT)
                .flatMap(r -> r.getOptional(net.minecraft.enchantment.Enchantments.LOYALTY));
        for (int i = 0; i < 9; i++) {
            var stack = mc.player.getInventory().getStack(i);
            if (!stack.isOf(Items.TRIDENT)) continue;
            if (loyaltyKey.isEmpty()) return i;
            if (EnchantmentHelper.getLevel(loyaltyKey.get(), stack) > 0) return i;
        }
        return -1;
    }

    private PlayerEntity findNearestEnemy(int maxRange) {
        PlayerEntity nearest = null;
        double best = maxRange;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player || p.isDead() || p.getHealth() <= 0) continue;
            double d = mc.player.distanceTo(p);
            if (d < best) { best = d; nearest = p; }
        }
        return nearest;
    }
}
