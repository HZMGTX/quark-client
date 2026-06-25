package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class QuickShield2 extends Module {

    private final IntSetting reactionMs = register(new IntSetting(
            "Reaction Ms", "Milliseconds to hold shield after taking damage", 80, 20, 500));

    private final BoolSetting onProjectile = register(new BoolSetting(
            "On Projectile", "Only activate for projectile damage", false));

    private final TimerUtil shieldTimer = new TimerUtil();
    private boolean shielding = false;

    public QuickShield2() {
        super("QuickShield2", "Rapidly deploys shield on incoming hit", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        shielding = false;
        if (mc.options != null) {
            mc.options.useKey.setPressed(false);
        }
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;

        // Check projectile filter
        if (onProjectile.isEnabled()) {
            String sourceName = event.getSource().getName();
            boolean isProjectile = sourceName.contains("arrow") ||
                    sourceName.contains("trident") ||
                    sourceName.contains("thrown");
            if (!isProjectile) return;
        }

        // Check if player has a shield in either hand
        ItemStack main = mc.player.getMainHandStack();
        ItemStack off = mc.player.getOffHandStack();
        boolean hasShield = main.getItem() == Items.SHIELD || off.getItem() == Items.SHIELD;

        if (hasShield) {
            mc.options.useKey.setPressed(true);
            shielding = true;
            shieldTimer.reset();
        }
    }

    // Release shield after reactionMs via tick check
    @EventHandler
    public void onTick(cc.quark.event.events.EventTick event) {
        if (!shielding) return;
        if (shieldTimer.hasReached(reactionMs.get())) {
            mc.options.useKey.setPressed(false);
            shielding = false;
        }
    }
}
