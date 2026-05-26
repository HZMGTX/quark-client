package cc.quark.event.events;
import cc.quark.event.Event;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockState;
public class EventBlockBreak extends Event {
    private final BlockPos pos;
    private final BlockState state;
    public EventBlockBreak(BlockPos pos, BlockState state) { this.pos = pos; this.state = state; }
    public BlockPos getPos() { return pos; }
    public BlockState getState() { return state; }
    @Override public boolean isCancellable() { return false; }
}
