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
import net.minecraft.util.math.BlockPos;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

/**
 * SignRead - Reads nearby sign text and optionally logs it to a file.
 */
public class SignRead extends Module {

    private final DoubleSetting range = register(new DoubleSetting(
            "Range", "Radius to scan for signs", 8.0, 2.0, 16.0));
    private final BoolSetting logFile = register(new BoolSetting(
            "LogFile", "Write sign contents to quark/signs.log", false));

    private final TimerUtil timer = new TimerUtil();
    private final Set<BlockPos> logged = new HashSet<>();
    private FileWriter writer;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SignRead() {
        super("SignRead", "Reads and logs nearby sign text", Category.WORLD);
    }

    @Override
    public void onEnable() {
        logged.clear();
        if (logFile.isEnabled()) openWriter();
        timer.reset();
    }

    @Override
    public void onDisable() {
        closeWriter();
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
            if (pos.getSquaredDistance(center) > rangeSq) continue;
            if (logged.contains(pos)) continue;

            BlockEntity be = mc.world.getBlockEntity(pos);
            if (!(be instanceof SignBlockEntity sign)) continue;

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                var text = sign.getFrontText().getMessage(i, false);
                String line = text.getString().trim();
                if (!line.isEmpty()) sb.append(line).append(" ");
            }

            String content = sb.toString().trim();
            if (content.isEmpty()) {
                logged.add(pos.toImmutable()); // Don't re-check blank signs
                continue;
            }

            String msg = "Sign at " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ": " + content;
            ChatUtil.info(msg);
            logged.add(pos.toImmutable());

            if (logFile.isEnabled()) writeLog(msg);
        }
    }

    private void openWriter() {
        try {
            Path logPath = mc.runDirectory.toPath().resolve("quark").resolve("signs.log");
            Files.createDirectories(logPath.getParent());
            writer = new FileWriter(logPath.toFile(), true);
        } catch (IOException e) {
            writer = null;
        }
    }

    private void closeWriter() {
        if (writer != null) {
            try { writer.close(); } catch (IOException ignored) {}
            writer = null;
        }
    }

    private void writeLog(String msg) {
        if (writer == null) return;
        try {
            writer.write("[" + LocalDateTime.now().format(FMT) + "] " + msg + System.lineSeparator());
            writer.flush();
        } catch (IOException ignored) {}
    }
}
