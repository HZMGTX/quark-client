package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class ThunderSword extends Module {

    private final BoolSetting onKill = register(new BoolSetting(
            "On Kill", "Summon lightning at the kill location", true));

    // Track entities at low health so we can detect the kill next tick
    private LivingEntity pendingTarget = null;
    private double pendingX, pendingY, pendingZ;

    public ThunderSword() {
        super("ThunderSword", "Summons a lightning bolt at the target's position on kill (cosmetic)", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        pendingTarget = null;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        // If we had a near-death target last tick, check if it died
        if (pendingTarget != null) {
            if (pendingTarget.isRemoved() || pendingTarget.getHealth() <= 0f) {
                if (onKill.isEnabled()) {
                    summonLightning(pendingX, pendingY, pendingZ);
                }
            }
            pendingTarget = null;
        }

        // Find the nearest low-health entity as a candidate
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof LivingEntity le)) continue;
            if (ent == mc.player) continue;
            if (!(le instanceof PlayerEntity)) continue;
            if (le.isRemoved()) continue;
            if (le.getHealth() <= 4f && mc.player.distanceTo(le) <= 6.0) {
                pendingTarget = le;
                pendingX = le.getX();
                pendingY = le.getY();
                pendingZ = le.getZ();
                break;
            }
        }
    }

    private void summonLightning(double x, double y, double z) {
        if (mc.player == null) return;
        // Creative/OP command — cosmetic only in survival
        mc.player.networkHandler.sendCommand(
                String.format("summon lightning_bolt %.2f %.2f %.2f", x, y, z));
    }
}
