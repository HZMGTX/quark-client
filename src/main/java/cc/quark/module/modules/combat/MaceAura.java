package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.setting.ModeSetting;
import cc.quark.util.TimerUtil;
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
 * MaceAura — uses the 1.21 mace with enhanced attack patterns.
 * Supports fall-damage bonus timing, multi-hit sweeps, and target prioritization.
 */
public class MaceAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range in blocks", 4.5, 1.0, 8.0));
    private final IntSetting delay = register(new IntSetting(
            "Delay", "Milliseconds between attacks", 200, 50, 1000));
    private final ModeSetting priority = register(new ModeSetting(
            "Priority", "Target selection mode", "Closest", "Closest", "Lowest HP", "Players First"));
    private final DoubleSetting fallBonus = register(new DoubleSetting(
            "Fall Bonus Threshold", "Min fall speed (m/s) for bonus damage", 0.5, 0.0, 5.0));
    private final BoolSetting waitForBonus = register(new BoolSetting(
            "Wait For Bonus", "Only attack when falling fast enough for bonus", false));
    private final BoolSetting targetPlayers = register(new BoolSetting(
            "Players", "Target players", true));
    private final BoolSetting targetHostiles = register(new BoolSetting(
            "Hostiles", "Target hostile mobs", true));
    private final BoolSetting autoSwitch = register(new BoolSetting(
            "Auto Switch", "Switch to mace slot automatically", true));
    private final BoolSetting switchBack = register(new BoolSetting(
            "Switch Back", "Return to previous slot after each attack", true));

    private final TimerUtil timer = new TimerUtil();

    public MaceAura() {
        super("MaceAura", "Attacks with the 1.21 mace using enhanced fall-damage patterns", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        mc.getEventBus().subscribe(this);
        timer.reset();
    }

    @Override
    public void onDisable() {
        mc.getEventBus().unsubscribe(this);
    }

    @Override
    public String getSuffix() {
        return priority.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delay.get())) return;

        // Fall speed check (velocity.y is negative when falling)
        double fallSpeed = -mc.player.getVelocity().y;
        if (waitForBonus.isEnabled() && fallSpeed < fallBonus.get()) return;

        // Ensure mace is in hand
        boolean holdingMace = mc.player.getMainHandStack().getItem() == Items.MACE;
        int maceSlot = -1;
        if (!holdingMace) {
            if (!autoSwitch.isEnabled()) return;
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).getItem() == Items.MACE) {
                    maceSlot = i;
                    break;
                }
            }
            if (maceSlot == -1) return;
        }

        LivingEntity target = findTarget();
        if (target == null) return;

        int prevSlot = mc.player.getInventory().selectedSlot;
        if (!holdingMace) mc.player.getInventory().selectedSlot = maceSlot;

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();

        if (switchBack.isEnabled() && !holdingMace) {
            mc.player.getInventory().selectedSlot = prevSlot;
        }
    }

    private LivingEntity findTarget() {
        double r = range.get();
        Box box = mc.player.getBoundingBox().expand(r, r, r);
        List<LivingEntity> candidates = new ArrayList<>();

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

        switch (priority.get()) {
            case "Lowest HP" -> candidates.sort(Comparator.comparingDouble(LivingEntity::getHealth));
            case "Players First" -> candidates.sort((a, b) -> {
                boolean ap = a instanceof PlayerEntity;
                boolean bp = b instanceof PlayerEntity;
                if (ap && !bp) return -1;
                if (!ap && bp) return 1;
                return Double.compare(mc.player.distanceTo(a), mc.player.distanceTo(b));
            });
            default -> candidates.sort(Comparator.comparingDouble(e -> mc.player.distanceTo(e)));
        }

        return candidates.get(0);
    }
}
