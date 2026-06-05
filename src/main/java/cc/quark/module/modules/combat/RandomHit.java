package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * RandomHit — attacks with randomised timing (Delay ± Jitter ms) and picks
 * a random in-range target each cycle, making the pattern harder to detect.
 */
public class RandomHit extends Module {

    private final DoubleSetting range  = register(new DoubleSetting("Range",  "Attack range",                       3.5,  1.0, 6.0));
    private final IntSetting    delay  = register(new IntSetting   ("Delay",  "Base ms between attacks",            250, 50, 1000));
    private final IntSetting    jitter = register(new IntSetting   ("Jitter", "Max ± ms random jitter on delay",    80,   0, 300));

    private final TimerUtil timer  = new TimerUtil();
    private final Random    random = new Random();
    private long nextDelay = 250;

    public RandomHit() {
        super("RandomHit", "Attacks random targets with randomised timing", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        nextDelay = delay.get();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(nextDelay)) return;

        List<LivingEntity> targets = new ArrayList<>();
        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!(entity instanceof LivingEntity living)) continue;
            if (living.isRemoved() || living.getHealth() <= 0f) continue;
            if (mc.player.distanceTo(entity) > range.get()) continue;
            targets.add(living);
        }

        if (!targets.isEmpty()) {
            LivingEntity target = targets.get(random.nextInt(targets.size()));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            timer.reset();
            int j = jitter.get();
            nextDelay = delay.get() + (j > 0 ? (long)(random.nextDouble() * 2 * j - j) : 0);
            nextDelay = Math.max(50, nextDelay);
        }
    }
}
