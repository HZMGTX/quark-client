package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.item.AxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class GoldFarm extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Attack range for zombified piglins", 4.5, 2.0, 8.0));
    private final IntSetting attackDelay = register(new IntSetting(
            "Attack Delay", "Delay between attacks (ms)", 600, 100, 2000));
    private final BoolSetting requireWeapon = register(new BoolSetting(
            "Require Weapon", "Only attack when holding a sword or axe", true));
    private final BoolSetting collectDrops = register(new BoolSetting(
            "Collect Drops", "Move toward nearby gold/loot drops", true));
    private final TimerUtil timer = new TimerUtil();

    public GoldFarm() {
        super("GoldFarm", "Assists with gold farm automation by killing zombified piglins", Category.WORLD);
    }

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (requireWeapon.isEnabled()) {
            var held = mc.player.getMainHandStack().getItem();
            if (!(held instanceof SwordItem) && !(held instanceof AxeItem)) return;
        }

        if (!timer.hasReached(attackDelay.get())) return;

        Box box = mc.player.getBoundingBox().expand(range.get());
        List<ZombifiedPiglinEntity> targets = mc.world.getEntitiesByClass(
                ZombifiedPiglinEntity.class, box,
                e -> !e.isDead() && mc.player.squaredDistanceTo(e) <= range.get() * range.get());

        if (targets.isEmpty()) return;

        targets.sort(Comparator.comparingDouble(e -> mc.player.squaredDistanceTo(e)));
        ZombifiedPiglinEntity target = targets.get(0);

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
    }
}
