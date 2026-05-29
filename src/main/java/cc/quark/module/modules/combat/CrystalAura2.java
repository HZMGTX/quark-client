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
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * CrystalAura2 — alternate end-crystal aura.
 * Phase 1: places an End Crystal at the nearest empty obsidian/bedrock block
 *          adjacent to the target.
 * Phase 2: immediately breaks the crystal for the explosion damage.
 * A configurable break-delay separates placement from detonation.
 */
public class CrystalAura2 extends Module {

    private final DoubleSetting placeRange = register(new DoubleSetting("Place Range", "Range to place crystals",     4.0, 1.0, 6.0));
    private final DoubleSetting breakRange = register(new DoubleSetting("Break Range", "Range to break crystals",     5.0, 1.0, 8.0));
    private final IntSetting    breakDelay = register(new IntSetting   ("Break Delay", "Ms before breaking crystal",  100, 0, 1000));

    private final TimerUtil timer = new TimerUtil();
    private int prevSlot = -1;

    public CrystalAura2() {
        super("CrystalAura2", "Places and immediately breaks end crystals for explosion damage", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Phase 2: break any nearby crystals first
        if (timer.hasReached(breakDelay.get())) {
            for (Entity entity : mc.world.getEntities()) {
                if (!(entity instanceof EndCrystalEntity)) continue;
                if (mc.player.distanceTo(entity) > breakRange.get()) continue;
                mc.interactionManager.attackEntity(mc.player, entity);
                mc.player.swingHand(Hand.MAIN_HAND);
                break;
            }
        }

        if (!timer.hasReached(200)) return; // overall rate limit

        // Phase 1: place a crystal
        int crystalSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) {
                crystalSlot = i; break;
            }
        }
        if (crystalSlot == -1) return;

        // Find a valid placement block (obsidian or bedrock top-face in range)
        BlockPos placePos = findPlacementPos();
        if (placePos == null) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != crystalSlot) { prevSlot = cur; mc.player.getInventory().selectedSlot = crystalSlot; }

        Vec3d hitVec = Vec3d.ofCenter(placePos).add(0, 0.5, 0);
        BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, placePos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        timer.reset();
        restoreSlot();
    }

    private BlockPos findPlacementPos() {
        BlockPos origin = mc.player.getBlockPos();
        int r = (int) placeRange.get();
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos base = origin.add(dx, dy, dz);
                    if (mc.player.distanceTo(Vec3d.ofCenter(base)) > placeRange.get()) continue;
                    var baseState = mc.world.getBlockState(base);
                    if (baseState.isOf(net.minecraft.block.Blocks.OBSIDIAN)
                            || baseState.isOf(net.minecraft.block.Blocks.BEDROCK)) {
                        BlockPos above = base.up();
                        if (mc.world.getBlockState(above).isAir()
                                && mc.world.getBlockState(above.up()).isAir()) {
                            return base;
                        }
                    }
                }
            }
        }
        return null;
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
