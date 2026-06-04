package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class CrystalSwitch2 extends Module {

    private final IntSetting    switchMs = register(new IntSetting("SwitchMs",  "Minimum ms between switches",   100, 50, 1000));
    private final DoubleSetting range    = register(new DoubleSetting("Range",  "Crystal interaction range",      4.0, 2.0, 8.0));

    private long lastSwitchTime = 0L;
    private int  savedSlot      = -1;

    public CrystalSwitch2() {
        super("CrystalSwitch2", "Enhanced crystal switch with timing", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        lastSwitchTime = 0L;
        savedSlot      = -1;
    }

    @Override
    public void onDisable() {
        // Restore original slot if we switched
        if (savedSlot >= 0 && mc.player != null) {
            mc.player.getInventory().selectedSlot = savedSlot;
            savedSlot = -1;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastSwitchTime < switchMs.get()) return;

        // Find nearest end crystal
        EndCrystalEntity nearest = null;
        double bestDist = range.get();
        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            double d = mc.player.distanceTo(crystal);
            if (d < bestDist) {
                bestDist = d;
                nearest  = crystal;
            }
        }

        if (nearest == null) {
            // No crystal in range — restore slot
            if (savedSlot >= 0) {
                mc.player.getInventory().selectedSlot = savedSlot;
                savedSlot = -1;
            }
            return;
        }

        // Find crystal in hotbar
        int crystalSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) {
                crystalSlot = i;
                break;
            }
        }

        if (crystalSlot >= 0 && mc.player.getInventory().selectedSlot != crystalSlot) {
            if (savedSlot < 0) savedSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = crystalSlot;
            lastSwitchTime = now;
        }
    }
}
