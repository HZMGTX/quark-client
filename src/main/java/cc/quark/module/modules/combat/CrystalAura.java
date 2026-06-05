package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class CrystalAura extends Module {
    private final DoubleSetting range = register(new DoubleSetting("Range", "Crystal aura range", 5.0, 1.0, 10.0));
    private final BoolSetting autoPlace = register(new BoolSetting("Auto Place", "Auto-place crystals", true));
    private final BoolSetting autoBreak = register(new BoolSetting("Auto Break", "Auto-break crystals", true));
    private final TimerUtil placeTimer = new TimerUtil();
    private final TimerUtil breakTimer = new TimerUtil();

    public CrystalAura() { super("CrystalAura", "Auto-places and detonates end crystals on enemies", Category.COMBAT); }
    @Override public void onEnable() { mc.getEventBus().subscribe(this); }
    @Override public void onDisable() { mc.getEventBus().unsubscribe(this); }

    @EventHandler
    public void onTick(EventTick e) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        PlayerEntity target = getTarget();
        if (target == null) return;

        if (autoBreak.isEnabled() && breakTimer.hasReached(50)) {
            for (var ent : mc.world.getEntities()) {
                if (!(ent instanceof EndCrystalEntity crystal)) continue;
                if (target.distanceTo(crystal) > 4) continue;
                mc.interactionManager.attackEntity(mc.player, crystal);
                mc.player.swingHand(Hand.MAIN_HAND);
                breakTimer.reset();
                return;
            }
        }

        if (autoPlace.isEnabled() && placeTimer.hasReached(100) && hasEndCrystal()) {
            BlockPos targetPos = target.getBlockPos();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos placePos = targetPos.add(dx, 0, dz);
                    var state = mc.world.getBlockState(placePos);
                    if (state.getBlock() != Blocks.OBSIDIAN && state.getBlock() != Blocks.BEDROCK) continue;
                    BlockPos above = placePos.up();
                    if (!mc.world.getBlockState(above).isAir()) continue;
                    if (mc.player.distanceTo(Vec3d.ofCenter(above)) > range.get()) continue;
                    switchToEndCrystal();
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                        new BlockHitResult(Vec3d.ofCenter(placePos).add(0, 0.5, 0), Direction.UP, placePos, false));
                    placeTimer.reset();
                    return;
                }
            }
        }
    }

    private PlayerEntity getTarget() {
        PlayerEntity closest = null; double dist = range.get();
        for (var ent : mc.world.getEntities()) {
            if (!(ent instanceof PlayerEntity pe) || pe == mc.player) continue;
            double d = mc.player.distanceTo(pe);
            if (d < dist) { dist = d; closest = pe; }
        }
        return closest;
    }

    private boolean hasEndCrystal() {
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) return true;
        return false;
    }

    private void switchToEndCrystal() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                mc.player.getInventory().selectedSlot = i; return;
            }
        }
    }
}
