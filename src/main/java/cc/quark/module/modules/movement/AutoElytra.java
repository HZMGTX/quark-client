package cc.quark.module.modules.movement;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class AutoElytra extends Module {

    private final BoolSetting autoFire = register(new BoolSetting(
            "AutoFire", "Fire a firework from inventory while elytra gliding", false));

    private boolean wasFlying = false;

    public AutoElytra() {
        super("AutoElytra", "Auto-equips elytra when falling and unequips on landing", Category.MOVEMENT);
    }

    @Override
    public void onEnable() {
        wasFlying = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        boolean onGround = mc.player.isOnGround();
        boolean falling = !onGround && mc.player.getVelocity().y < -0.1;
        boolean hasElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
        boolean isGliding = mc.player.isFallFlying();

        if (falling && hasElytra && !isGliding) {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(
                        new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        if (autoFire.isEnabled() && isGliding) {
            int slot = findFirework();
            if (slot != -1) {
                int prev = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = slot;
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, mc.player.getYaw(), mc.player.getPitch()));
                }
                mc.player.getInventory().selectedSlot = prev;
            }
        }

        wasFlying = isGliding;
    }

    private int findFirework() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.FIREWORK_ROCKET) return i;
        }
        return -1;
    }
}
