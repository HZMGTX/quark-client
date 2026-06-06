package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * AutoMace — automatically attacks targets with the 1.21 mace weapon,
 * timing attacks for maximum fall-damage bonus.
 */
public class AutoMace extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 4.5, 1.0, 8.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between attacks", 150, 50, 1000));
    private final BoolSetting onlyFalling = register(new BoolSetting(
            "Only Falling", "Attack only when falling for mace bonus", true));
    private final BoolSetting switchToMace = register(new BoolSetting(
            "Auto Switch", "Switch to mace slot automatically", true));
    private final BoolSetting switchBack = register(new BoolSetting(
            "Switch Back", "Switch back after attacking", true));
    private final BoolSetting targetPlayers = register(new BoolSetting(
            "Players", "Target players", true));
    private final BoolSetting targetHostiles = register(new BoolSetting(
            "Hostiles", "Target hostile mobs", true));

    private long lastAttack = 0;

    public AutoMace() {
        super("AutoMace", "Auto-attacks with the 1.21 mace weapon for maximum impact", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastAttack < delay.get()) return;

        // Check if we should only attack while falling
        if (onlyFalling.isEnabled() && mc.player.getVelocity().y >= -0.1) return;

        // Find mace slot
        int maceSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.MACE) {
                maceSlot = i;
                break;
            }
        }

        // Check if currently holding mace or auto-switch is enabled
        boolean holdingMace = mc.player.getMainHandStack().getItem() == Items.MACE;
        if (!holdingMace && (!switchToMace.isEnabled() || maceSlot == -1)) return;

        // Find best target
        LivingEntity target = findTarget();
        if (target == null) return;

        int prevSlot = mc.player.getInventory().selectedSlot;

        if (!holdingMace && maceSlot != -1) {
            mc.player.getInventory().selectedSlot = maceSlot;
        }

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = System.currentTimeMillis();

        if (switchBack.isEnabled() && !holdingMace) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    private LivingEntity findTarget() {
        List<LivingEntity> candidates = new ArrayList<>();
        double r = range.get();
        Box box = mc.player.getBoundingBox().expand(r, r, r);

        for (Entity entity : mc.world.getEntitiesByClass(LivingEntity.class, box, e -> true)) {
            if (entity == mc.player) continue;
            if (entity.isRemoved() || entity.getHealth() <= 0f) continue;
            if (entity instanceof PlayerEntity && !targetPlayers.isEnabled()) continue;
            if (entity instanceof HostileEntity && !targetHostiles.isEnabled()) continue;
            if (!(entity instanceof PlayerEntity) && !(entity instanceof HostileEntity)) continue;
            if (mc.player.distanceTo(entity) > r) continue;
            candidates.add((LivingEntity) entity);
        }

        if (candidates.isEmpty()) return null;
        candidates.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        return candidates.get(0);
    }
}
