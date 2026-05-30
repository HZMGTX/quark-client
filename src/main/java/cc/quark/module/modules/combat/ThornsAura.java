package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Hand;

/**
 * ThornsAura — when the player takes damage and has Thorns on armor, retaliates
 * by attacking the source entity within range.
 */
public class ThornsAura extends Module {

    private final BoolSetting   autoCounter = register(new BoolSetting  ("AutoCounter", "Counter-attack on damage",        true));
    private final DoubleSetting range       = register(new DoubleSetting("Range",       "Max range to counter-attack",     5.0, 1.0, 10.0));
    private final IntSetting    cooldown    = register(new IntSetting   ("Cooldown",    "Cooldown in ms between counters", 500, 100, 2000));

    private final TimerUtil timer = new TimerUtil();

    public ThornsAura() {
        super("ThornsAura", "Retaliates against attackers when the player has Thorns armor", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (!autoCounter.isEnabled()) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(cooldown.get())) return;

        // Check if player has Thorns on any armor piece
        boolean hasThorns = false;
        for (EquipmentSlot slot : new EquipmentSlot[]{
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            var stack = mc.player.getEquippedStack(slot);
            if (!stack.isEmpty()) {
                var registry = mc.world.getRegistryManager();
                // Check via item having thorns by inspecting NBT level
                if (stack.getEnchantments().getLevel("thorns") > 0) {
                    hasThorns = true;
                    break;
                }
            }
        }
        if (!hasThorns) return;

        DamageSource src = event.getSource();
        Entity attacker  = src.getAttacker();
        if (attacker == null) attacker = src.getSource();
        if (attacker == null || !(attacker instanceof LivingEntity)) return;

        if (mc.player.distanceTo(attacker) > range.get()) return;

        mc.interactionManager.attackEntity(mc.player, attacker);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }
}
