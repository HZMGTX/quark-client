package cc.quark.module.modules.world;

import cc.quark.event.EventHandler;
import cc.quark.event.events.EventTick;
import cc.quark.module.Category;
import cc.quark.module.Module;
import cc.quark.setting.BoolSetting;
import cc.quark.setting.DoubleSetting;
import cc.quark.util.ChatUtil;
import cc.quark.util.TimerUtil;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;

public class SignReader extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Radius to scan for sign blocks", 6.0, 1.0, 16.0));
    private final BoolSetting logChat = register(new BoolSetting(
            "LogChat", "Print sign text to chat", true));

    private final Set<BlockPos> readSigns = new HashSet<>();
    private final TimerUtil timer = new TimerUtil();

    public SignReader() {
        super("SignReader", "Reads and logs nearby sign text to chat", Category.WORLD);
    }

    @Override
    public void onEnable() {
        readSigns.clear();
        timer.reset();
    }

    @EventHandler
    public void onTick(EventTick event) {
        if (mc.player == null || mc.world == null) return;
        if (!timer.hasReached(500)) return;
        timer.reset();

        double rangeSq = range.get() * range.get();
        BlockPos center = mc.player.getBlockPos();
        int r = (int) Math.ceil(range.get());

        for (BlockPos pos : BlockPos.iterate(center.add(-r, -r, -r), center.add(r, r, r))) {
            if (pos.getSquaredDistance(mc.player.getPos()) > rangeSq) continue;
            if (readSigns.contains(pos)) continue;

            BlockEntity be = mc.world.getBlockEntity(pos);
            if (!(be instanceof SignBlockEntity sign)) continue;

            readSigns.add(pos.toImmutable());

            if (logChat.isEnabled()) {
                StringBuilder sb = new StringBuilder("Sign at " + pos.toShortString() + ": ");
                for (int i = 0; i < 4; i++) {
                    Text line = sign.getFrontText().getMessage(i, false);
                    String txt = line.getString().trim();
                    if (!txt.isEmpty()) sb.append("[").append(txt).append("] ");
                }
                String message = sb.toString().trim();
                if (!message.endsWith(":")) {
                    ChatUtil.info(message);
                }
            }
        }
    }
}
