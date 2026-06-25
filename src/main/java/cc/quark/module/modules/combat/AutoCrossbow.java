package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class AutoCrossbow extends Module {

    private final BoolSetting autoReload = register(new BoolSetting("AutoReload", "Reload crossbow automatically after firing", true));

    private final TimerUtil timer = new TimerUtil();
    private boolean charging = false;
    private int chargeStartTick = 0;
    private int tickCount = 0;

    public AutoCrossbow() {
        super("AutoCrossbow", "Charges and fires crossbow automatically at nearest enemy", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        charging = false;
        tickCount = 0;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        tickCount++;

        int crossbowSlot = findCrossbowSlot();
        if (crossbowSlot == -1) return;

        ItemStack crossbow = mc.player.getInventory().getStack(crossbowSlot);

        if (CrossbowItem.isCharged(crossbow)) {
            PlayerEntity target = findNearestEnemy(64.0);
            if (target == null) return;

            mc.player.getInventory().selectedSlot = crossbowSlot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(crossbowSlot));
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            charging = false;
        } else if (autoReload.isEnabled() && !mc.player.isUsingItem()) {
            mc.player.getInventory().selectedSlot = crossbowSlot;
            mc.options.useKey.setPressed(true);
            charging = true;
            chargeStartTick = tickCount;
        } else if (charging && tickCount - chargeStartTick > 25) {
            mc.options.useKey.setPressed(false);
            charging = false;
        }
    }

    @Override
    public void onDisable() {
        mc.options.useKey.setPressed(false);
        charging = false;
    }

    private int findCrossbowSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.CROSSBOW)) return i;
        }
        return -1;
    }

    private PlayerEntity findNearestEnemy(double maxRange) {
        PlayerEntity nearest = null;
        double best = maxRange;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player || p.isDead() || p.getHealth() <= 0) continue;
            double d = mc.player.distanceTo(p);
            if (d < best) { best = d; nearest = p; }
        }
        return nearest;
    }
}
