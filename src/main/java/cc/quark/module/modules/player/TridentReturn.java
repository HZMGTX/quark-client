package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;

import java.util.List;

public class TridentReturn extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Detection radius (blocks) for returning trident", 5.0, 1.0, 16.0));

    public TridentReturn() {
        super("TridentReturn", "Auto-catches returning trident in free hand", Category.PLAYER);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Only active when offhand is empty or holding nothing useful
        if (!mc.player.getOffHandStack().isEmpty()
                && mc.player.getOffHandStack().getItem() != Items.AIR) return;

        double r = range.get();
        Box searchBox = mc.player.getBoundingBox().expand(r);

        List<TridentEntity> tridents = mc.world.getEntitiesByClass(
                TridentEntity.class, searchBox,
                t -> t.getOwner() == mc.player && t.isReturning());

        if (tridents.isEmpty()) return;

        TridentEntity nearest = tridents.stream()
                .min((a, b) -> Double.compare(
                        a.squaredDistanceTo(mc.player),
                        b.squaredDistanceTo(mc.player)))
                .orElse(null);

        if (nearest == null) return;

        // Interact with offhand to be ready to catch
        mc.interactionManager.interactEntity(mc.player, nearest, Hand.OFF_HAND);
    }
}
