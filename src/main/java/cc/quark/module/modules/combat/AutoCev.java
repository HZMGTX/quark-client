package cc.quark.module.modules.combat;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventDamage;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.DoubleSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.TimerUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

/**
 * AutoCev — when taking crystal/explosion damage, places an obsidian block
 * directly below the player to CEV (Crystal Escape Vector) trap defence.
 */
public class AutoCev extends Module {

    private final DoubleSetting threshold = register(new DoubleSetting("Threshold", "Minimum damage to trigger placement", 4.0, 1.0, 20.0));
    private final IntSetting    cooldown  = register(new IntSetting   ("Cooldown",  "Ms cooldown between placements",       500, 100, 3000));

    private final TimerUtil timer = new TimerUtil();
    private boolean triggered = false;
    private int prevSlot = -1;

    public AutoCev() {
        super("AutoCev", "Places obsidian below player when taking crystal damage", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        timer.reset();
        triggered = false;
        prevSlot = -1;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @EventHandler
    public void onDamage(EventDamage event) {
        if (mc.player == null) return;
        if (event.getAmount() >= threshold.get()) {
            triggered = true;
        }
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!triggered) return;
        if (!timer.hasReached(cooldown.get())) return;
        triggered = false;

        // Find obsidian in hotbar
        int obsSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.OBSIDIAN)) { obsSlot = i; break; }
        }
        if (obsSlot == -1) return;

        int cur = mc.player.getInventory().selectedSlot;
        if (cur != obsSlot) { prevSlot = cur; mc.player.getInventory().selectedSlot = obsSlot; }

        // Place below player
        BlockPos below = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(below).isAir()) {
            Vec3d hitVec = Vec3d.ofCenter(below).add(0, 0.5, 0);
            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, below, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
        }

        timer.reset();
        restoreSlot();
    }

    private void restoreSlot() {
        if (prevSlot != -1 && mc.player != null) {
            mc.player.getInventory().selectedSlot = prevSlot;
            prevSlot = -1;
        }
    }
}
