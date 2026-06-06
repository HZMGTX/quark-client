package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

public class CrystalPhase extends Module {
    private final IntSetting range = register(new IntSetting("Range", "Crystal interaction range", 5, 1, 10));
    private final BoolSetting autoAttack = register(new BoolSetting("Auto Attack", "Auto-attack visible crystals", true));

    public CrystalPhase() { super("CrystalPhase", "Interacts with end crystals through blocks", Category.COMBAT); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!autoAttack.isEnabled()) return;
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof EndCrystalEntity crystal)) continue;
            if (mc.player.distanceTo(crystal) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, crystal);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }
}
