package cc.quark.module.modules.misc;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.IntSetting;
import cc.quark.util.ChatUtil;
import net.minecraft.block.Blocks;
import net.minecraft.block.NoteBlock;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class NoteBlockPlayer extends Module {
    private final BoolSetting autoTune = register(new BoolSetting("Auto Tune", "Auto-tune note blocks to target pitch", true));
    private final IntSetting targetNote = register(new IntSetting("Note", "Target note (0-24)", 12, 0, 24));

    public NoteBlockPlayer() { super("NoteBlockPlayer", "Auto-tunes and plays noteblocks", Category.MISC); }

    @EventHandler
    public void onTick(EventTick e) {
        if (!autoTune.isEnabled() || mc.player == null || mc.world == null) return;
        if (mc.crosshairTarget instanceof BlockHitResult bhr) {
            BlockPos pos = bhr.getBlockPos();
            if (mc.world.getBlockState(pos).getBlock() == Blocks.NOTE_BLOCK) {
                int note = mc.world.getBlockState(pos).get(NoteBlock.NOTE);
                if (note != targetNote.get()) {
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND,
                        new BlockHitResult(Vec3d.ofCenter(pos), Direction.UP, pos, false));
                    ChatUtil.info("Tuning note block: " + note + " -> " + targetNote.get());
                }
            }
        }
    }
}
