package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.ModeSetting;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

public class ExplosionAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Range to use explosive items", 5.0, 1.0, 10.0));

    private final ModeSetting type = register(new ModeSetting(
            "Type", "Type of explosive to use", "TNT", "TNT", "Crystal", "Bed"));

    private long lastUse = 0;

    public ExplosionAura() {
        super("ExplosionAura", "Uses explosive items against enemies", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (System.currentTimeMillis() - lastUse < 500) return;

        double r = range.get();
        boolean hasTarget = java.util.stream.StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .anyMatch(e -> e instanceof LivingEntity living
                        && living != mc.player
                        && !living.isRemoved()
                        && mc.player.distanceTo(living) <= r);
        if (!hasTarget) return;

        // Find appropriate item in hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            boolean matches = switch (type.get()) {
                case "TNT" -> stack.getItem() == Items.TNT;
                case "Crystal" -> stack.getItem() == Items.END_CRYSTAL;
                case "Bed" -> stack.getItem() == Items.RED_BED;
                default -> false;
            };
            if (matches) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = i;
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.getInventory().selectedSlot = prev;
                lastUse = System.currentTimeMillis();
                break;
            }
        }
    }
}
