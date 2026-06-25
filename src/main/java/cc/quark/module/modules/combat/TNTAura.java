package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.EntityUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FlintAndSteelItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;
import java.util.List;

public class TNTAura extends Module {

    private final DoubleSetting range = register(new DoubleSetting("Range", "Range to place TNT near enemies", 4.0, 2.0, 8.0));
    private final IntSetting delayMs = register(new IntSetting("Delay Ms", "Milliseconds between placements", 500, 100, 2000));
    private final BoolSetting requireFlint = register(new BoolSetting("Require Flint", "Require flint and steel to ignite", true));

    private final TimerUtil timer = new TimerUtil();

    public TNTAura() {
        super("TNTAura", "Auto-places and ignites TNT around enemies", Category.COMBAT);
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!timer.hasReached(delayMs.get())) return;

        List<LivingEntity> targets = EntityUtil.getEntitiesOfType(LivingEntity.class, range.get());
        targets.removeIf(e -> !(e instanceof PlayerEntity));
        targets.removeIf(EntityUtil::isFriend);
        if (targets.isEmpty()) return;

        targets.sort(Comparator.comparingDouble(EntityUtil::distanceTo));
        LivingEntity target = targets.get(0);

        // Find TNT in hotbar
        int tntSlot = -1;
        int flintSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TNT && tntSlot == -1) tntSlot = i;
            if (stack.getItem() instanceof FlintAndSteelItem && flintSlot == -1) flintSlot = i;
        }

        if (tntSlot == -1) return;
        if (requireFlint.isEnabled() && flintSlot == -1) return;

        // Find solid block below/near target to place TNT on
        BlockPos targetBlock = BlockPos.ofFloored(target.getPos()).down();
        if (!mc.world.getBlockState(targetBlock).isSolid()) return;

        BlockPos placePos = targetBlock.up();
        if (!mc.world.getBlockState(placePos).isAir()) return;

        int prevSlot = mc.player.getInventory().selectedSlot;

        // Place TNT
        mc.player.getInventory().selectedSlot = tntSlot;
        BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(targetBlock).add(0, 0.5, 0),
                Direction.UP, targetBlock, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);

        // Ignite with flint and steel
        if (flintSlot != -1) {
            mc.player.getInventory().selectedSlot = flintSlot;
            BlockHitResult igniteHit = new BlockHitResult(
                    Vec3d.ofCenter(placePos).add(0, 0.5, 0),
                    Direction.UP, placePos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, igniteHit);
        }

        mc.player.getInventory().selectedSlot = prevSlot;
        timer.reset();
    }
}
