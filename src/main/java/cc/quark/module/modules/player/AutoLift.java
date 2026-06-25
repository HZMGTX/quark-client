package cc.quark.module.modules.player;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoLift — auto-places a block beneath the player's feet when they are
 * falling into the void (Y < voidThreshold).
 */
public class AutoLift extends Module {

    private final DoubleSetting voidThreshold = register(new DoubleSetting(
            "Void Y", "Place block when player falls below this Y level", 0.0, -64.0, 64.0));

    private final BoolSetting switchBack = register(new BoolSetting(
            "Switch Back", "Return to previous hotbar slot after placing", true));

    private final BoolSetting notifyPlace = register(new BoolSetting(
            "Notify", "Send a chat notification when a block is placed", false));

    private final TimerUtil timer = new TimerUtil();
    private int savedSlot = -1;
    private boolean didPlace = false;

    public AutoLift() {
        super("AutoLift", "Auto-places block below feet when falling in void", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        savedSlot = -1;
        didPlace = false;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && savedSlot != -1 && switchBack.isEnabled()) {
            mc.player.getInventory().selectedSlot = savedSlot;
        }
        savedSlot = -1;
        didPlace = false;
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        ClientPlayerEntity player = mc.player;

        // Only act when falling below the void threshold
        if (player.getY() >= voidThreshold.get()) {
            // Restore slot once safely above threshold
            if (didPlace && savedSlot != -1 && switchBack.isEnabled()) {
                player.getInventory().selectedSlot = savedSlot;
                savedSlot = -1;
                didPlace = false;
            }
            return;
        }

        // Rate-limit to one place attempt per 100 ms
        if (!timer.hasReached(100)) return;
        timer.reset();

        // Find a block item in the hotbar
        int blockSlot = findBlockSlot(player);
        if (blockSlot == -1) return;

        // Save current slot and switch to the block
        if (savedSlot == -1) savedSlot = player.getInventory().selectedSlot;
        player.getInventory().selectedSlot = blockSlot;

        // Place directly below feet: target the top face of the block one below
        BlockPos belowPos = player.getBlockPos().down();
        // Make sure there's no solid block directly below already
        if (!mc.world.getBlockState(belowPos).isAir()) return;

        // We target the underside of the player's feet position
        Vec3d hitVec = new Vec3d(
                belowPos.getX() + 0.5,
                belowPos.getY() + 1.0,
                belowPos.getZ() + 0.5);

        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, belowPos, false);
        mc.interactionManager.interactBlock(player, Hand.MAIN_HAND, hitResult);
        player.swingHand(Hand.MAIN_HAND);
        didPlace = true;

        if (notifyPlace.isEnabled()) {
            ChatUtil.info("AutoLift: placed block at Y=" + belowPos.getY());
        }
    }

    private int findBlockSlot(ClientPlayerEntity player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                // Prefer solid, non-falling blocks
                BlockItem bi = (BlockItem) stack.getItem();
                if (bi.getBlock() != Blocks.SAND
                        && bi.getBlock() != Blocks.GRAVEL
                        && bi.getBlock() != Blocks.ANVIL) {
                    return i;
                }
            }
        }
        // Fallback: any block item
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                return i;
            }
        }
        return -1;
    }
}
