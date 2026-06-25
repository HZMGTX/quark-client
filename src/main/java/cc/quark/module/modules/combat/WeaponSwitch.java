package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventAttack;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.ModeSetting;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MaceItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;

public class WeaponSwitch extends Module {

    private final ModeSetting prefer = register(new ModeSetting("Prefer", "Preferred weapon type", "Sword", "Sword", "Axe", "Mace"));

    public WeaponSwitch() {
        super("WeaponSwitch", "Auto-switches to the best weapon in hotbar when attacking", Category.COMBAT);
    }

    private boolean isPreferred(ItemStack stack) {
        return switch (prefer.get()) {
            case "Axe" -> stack.getItem() instanceof AxeItem;
            case "Mace" -> stack.getItem() instanceof MaceItem;
            default -> stack.getItem() instanceof SwordItem;
        };
    }

    @EventHandler
    public void onAttack(EventAttack event) {
        if (mc.player == null) return;
        if (isPreferred(mc.player.getMainHandStack())) return;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isPreferred(stack)) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(i));
                mc.player.getInventory().selectedSlot = i;
                return;
            }
        }
    }
}
