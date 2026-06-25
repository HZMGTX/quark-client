package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class ThornsReflect extends Module {

    private final DoubleSetting damage = register(new DoubleSetting(
            "Damage", "Minimum incoming damage to trigger shield", 4.0, 1.0, 20.0));

    private final BoolSetting autoShield = register(new BoolSetting(
            "Auto Shield", "Automatically equip shield when projectile is near", true));

    private boolean wasShielding = false;

    public ThornsReflect() {
        super("ThornsReflect", "Uses shield to reflect incoming projectile damage", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.useKey.setPressed(false);
        }
        wasShielding = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        if (!autoShield.isEnabled()) {
            wasShielding = false;
            mc.options.useKey.setPressed(false);
            return;
        }

        // Check for nearby projectiles threatening the player
        boolean projectileNear = false;
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof ProjectileEntity)) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist > 6.0) continue;

            // Check if it's heading toward us
            projectileNear = true;
            break;
        }

        // Also check if holding a shield
        ItemStack mainhand = mc.player.getMainHandStack();
        ItemStack offhand = mc.player.getOffHandStack();
        boolean hasShield = mainhand.getItem() == Items.SHIELD || offhand.getItem() == Items.SHIELD;

        if (projectileNear && hasShield) {
            mc.options.useKey.setPressed(true);
            wasShielding = true;
        } else if (wasShielding) {
            mc.options.useKey.setPressed(false);
            wasShielding = false;
        }
    }
}
