package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;

public class ComboKiller extends Module {

    private final IntSetting comboMs = register(new IntSetting("Combo Ms", "Milliseconds between combo hits", 50, 20, 500));
    private final BoolSetting wTap = register(new BoolSetting("W-Tap", "Release W briefly to reset sprint for knockback", true));
    private final BoolSetting sTap = register(new BoolSetting("S-Tap", "Brief backward tap between attacks", false));

    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil tapTimer = new TimerUtil();
    private boolean tapping = false;

    public ComboKiller() {
        super("ComboKiller", "Performs W-tap combos during fights", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        tapping = false;
        attackTimer.reset();
        tapTimer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        tapping = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, 4.0);
        targets.removeIf(e -> !(e instanceof PlayerEntity));
        targets.removeIf(EntityUtil::isFriend);
        if (targets.isEmpty()) {
            if (tapping) {
                mc.options.forwardKey.setPressed(false);
                mc.options.backKey.setPressed(false);
                tapping = false;
            }
            return;
        }

        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        LivingEntity target = targets.get(0);

        // Handle tap release
        if (tapping && tapTimer.hasReached(50)) {
            mc.options.forwardKey.setPressed(true);
            mc.options.backKey.setPressed(false);
            tapping = false;
        }

        if (!attackTimer.hasReached(comboMs.get())) return;

        float cooldown = mc.player.getAttackCooldownProgress(0f);
        if (cooldown < 0.9f) return;

        // Attack
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        attackTimer.reset();

        // W-tap
        if (wTap.isEnabled() && mc.player.isSprinting()) {
            mc.options.forwardKey.setPressed(false);
            mc.player.setSprinting(false);
            tapping = true;
            tapTimer.reset();
        }

        // S-tap
        if (sTap.isEnabled() && !tapping) {
            mc.options.backKey.setPressed(true);
            mc.options.forwardKey.setPressed(false);
            tapping = true;
            tapTimer.reset();
        }
    }
}
