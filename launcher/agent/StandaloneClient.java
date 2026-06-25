package cc.quark.agent;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Self-contained Quark client that runs purely from the injected agent —
 * no Fabric mod, no mods folder. Provides a keyboard-driven ClickGUI plus a
 * live HUD, rendered with McReflect against the running Minecraft DrawContext.
 *
 * Every module here does something real:
 *   • Render  — FullBright (gamma), Zoom (FOV) are live game-option edits.
 *   • HUD     — Watermark, ModuleList, FPS, Keystrokes/CPS, Coordinates,
 *               ArmorStatus, Ping, Direction and Clock all read live state.
 *   • Misc    — ClickGui, ConfigManager (autosave) and Notifications drive
 *               the client itself.
 *
 * There is intentionally no combat/movement automation: this is a HUD and
 * quality-of-life client, not a cheat. Anything that could not be implemented
 * honestly was removed rather than left as a dead toggle.
 *
 * Controls: Right-Shift opens/closes the menu. ←/→ switch category, ↑/↓ move,
 * Enter toggles. [ and ] shrink/grow the whole UI (scale is saved).
 */
public final class StandaloneClient {

    private StandaloneClient() {}

    // ── Theme (ARGB) ──────────────────────────────────────────────────────────
    // Accent colours are swappable in game (press T with the menu open). Static
    // chrome and status colours stay fixed.
    private static int ACCENT      = 0xFF22D3EE;
    private static int ACCENT_DARK = 0xFF0E7490;
    private static int TEXT_ON     = 0xFF67E8F9;
    private static final int HEADER_BG   = 0xFF12161F;
    private static final int PANEL       = 0xF00B0E14;
    private static final int ROW         = 0xFF161B22;
    private static final int ROW_SEL     = 0xFF1F2733;
    private static final int TEXT        = 0xFFE6EDF3;
    private static final int TEXT_DIM    = 0xFF7D8590;
    private static final int BAR_GOOD    = 0xFF34D399;
    private static final int BAR_WARN    = 0xFFF59E0B;
    private static final int BAR_BAD     = 0xFFEF4444;

    // Accent palettes. The last entry ("Rainbow") is animated per-frame.
    private static final String[] THEME_NAMES = {
            "Cyan", "Purple", "Green", "Crimson", "Amber", "Pink", "Rainbow" };
    private static final int[][] THEMES = {
            { 0xFF22D3EE, 0xFF0E7490, 0xFF67E8F9 }, // Cyan
            { 0xFFA855F7, 0xFF7C3AED, 0xFFC4B5FD }, // Purple
            { 0xFF34D399, 0xFF059669, 0xFF6EE7B7 }, // Green
            { 0xFFEF4444, 0xFFB91C1C, 0xFFFCA5A5 }, // Crimson
            { 0xFFF59E0B, 0xFFB45309, 0xFFFCD34D }, // Amber
            { 0xFFEC4899, 0xFFBE185D, 0xFFF9A8D4 }, // Pink
    };
    private static final int RAINBOW_INDEX = THEME_NAMES.length - 1;
    private static int themeIndex = 0;
    private static long themeHintUntil = 0L;

    // ── Module model ──────────────────────────────────────────────────────────
    public static final class Module {
        public final String name;
        public final String description;
        public boolean enabled;
        Module(String name, String description) { this.name = name; this.description = description; }
    }

    private static final Map<String, List<Module>> CATEGORIES = new LinkedHashMap<>();
    private static final List<String> CAT_NAMES = new ArrayList<>();

    // Modules that paint their own dedicated HUD widget, so they stay out of the generic list.
    private static final java.util.Set<String> SELF_RENDERING = java.util.Set.of(
            "Watermark", "ModuleList", "FPS", "FpsGraph", "Keystrokes", "CPS", "Coordinates",
            "ArmorStatus", "Ping", "Direction", "Clock", "Health", "Hunger",
            "Speed", "HeldItem", "ServerIP", "GameTime", "Memory", "SessionInfo");

    private static boolean guiOpen = false;
    private static int catIndex = 0;
    private static int modIndex = 0;
    private static boolean built = false;

    private static final Map<Integer, Boolean> prevKey = new LinkedHashMap<>();

    private static float guiAnim = 0f;
    private static long lastFrameNanos = 0L;
    private static int leftHudY = 4;

    // UI scale (persisted). 1.0 = native ClickGUI size.
    private static float guiScale = 1.0f;
    private static final float SCALE_MIN = 0.6f, SCALE_MAX = 1.8f, SCALE_STEP = 0.1f;
    private static long scaleHintUntil = 0L;

    private static Double savedGamma;
    private static boolean prevFullBright = false;
    private static Double savedFov;

    private static boolean prevMouseLeft = false;
    private static int cps = 0;
    private static final List<Long> leftClickTimes = new ArrayList<>();

    // Rolling FPS samples for the FpsGraph widget.
    private static final int[] fpsHistory = new int[60];
    private static int fpsHistoryLen = 0;
    private static long lastFpsSample = 0L;

    // Horizontal speed (blocks/sec), smoothed across frames.
    private static double[] lastSpeedPos = null;
    private static long lastSpeedNanos = 0L;
    private static double bps = 0.0;

    private static final DateTimeFormatter CLOCK_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final class Toast {
        final String text; final long start;
        Toast(String t) { text = t; start = System.currentTimeMillis(); }
    }
    private static final List<Toast> TOASTS = new ArrayList<>();
    private static final long TOAST_LIFETIME_MS = 2600;
    private static final long TOAST_FADE_MS     = 500;

    // ── Init ──────────────────────────────────────────────────────────────────

    public static void init(Object mc) {
        if (built) return;
        built = true;
        McReflect.bind(mc);
        buildModules();
        applyConfig();

        MinecraftHook.addRender2DListener(StandaloneClient::onRender);
        toast("QUARK ready — Right-Shift to open");
        System.out.println("[Quark Client] Standalone client ready — press Right-Shift in game to open the menu.");
    }

    private static void buildModules() {
        add("Render",   "FullBright",    "Maxes out brightness while enabled");
        add("Render",   "Zoom",          "Hold C to zoom the camera in");

        add("HUD",      "Watermark",     "Quark logo + FPS", true);
        add("HUD",      "ModuleList",    "Active module list", true);
        add("HUD",      "FPS",           "Standalone FPS counter");
        add("HUD",      "FpsGraph",      "Rolling FPS line graph");
        add("HUD",      "Keystrokes",    "WASD + mouse keys with live CPS", true);
        add("HUD",      "CPS",           "Standalone click-per-second counter");
        add("HUD",      "Coordinates",   "Live player XYZ position", true);
        add("HUD",      "ArmorStatus",   "Worn armor + durability");
        add("HUD",      "Ping",          "Live connection latency");
        add("HUD",      "Direction",     "Facing direction + yaw");
        add("HUD",      "Clock",         "Real-time system clock");
        add("HUD",      "Health",        "Live health + absorption");
        add("HUD",      "Hunger",        "Live food level");
        add("HUD",      "Speed",         "Horizontal speed in blocks/sec");
        add("HUD",      "HeldItem",      "Name + count of the held item");
        add("HUD",      "ServerIP",      "Address of the current server");
        add("HUD",      "GameTime",      "In-game day + clock");
        add("HUD",      "Memory",        "JVM heap usage");
        add("HUD",      "SessionInfo",   "Your username + FPS");

        add("Misc",     "ClickGui",      "This menu", true);
        add("Misc",     "ConfigManager", "Autosaves your settings", true);
        add("Misc",     "Notifications", "Toast pop-ups for toggles", true);
    }

    private static void add(String cat, String name, String desc) { add(cat, name, desc, false); }
    private static void add(String cat, String name, String desc, boolean on) {
        CATEGORIES.computeIfAbsent(cat, k -> { CAT_NAMES.add(k); return new ArrayList<>(); });
        Module m = new Module(name, desc);
        m.enabled = on;
        CATEGORIES.get(cat).add(m);
    }

    private static List<Module> current() { return CATEGORIES.get(CAT_NAMES.get(catIndex)); }

    // ── Config persistence ───────────────────────────────────────────────────

    private static void applyConfig() {
        Map<String, String> saved = QuarkConfig.load();
        if (saved.isEmpty()) return;
        for (Map.Entry<String, List<Module>> e : CATEGORIES.entrySet())
            for (Module m : e.getValue()) {
                String v = saved.get(e.getKey() + "." + m.name);
                if (v != null) m.enabled = Boolean.parseBoolean(v);
            }
        String s = saved.get("ui.scale");
        if (s != null) {
            try { guiScale = clampScale(Float.parseFloat(s)); } catch (NumberFormatException ignored) {}
        }
        String t = saved.get("ui.theme");
        if (t != null) {
            try { themeIndex = Math.floorMod(Integer.parseInt(t), THEME_NAMES.length); } catch (NumberFormatException ignored) {}
        }
        applyTheme();
    }

    private static void persist() {
        if (!isEnabled("Misc", "ConfigManager")) return;
        Map<String, String> states = new LinkedHashMap<>();
        for (Map.Entry<String, List<Module>> e : CATEGORIES.entrySet())
            for (Module m : e.getValue())
                states.put(e.getKey() + "." + m.name, String.valueOf(m.enabled));
        states.put("ui.scale", String.valueOf(guiScale));
        states.put("ui.theme", String.valueOf(themeIndex));
        QuarkConfig.save(states);
    }

    // ── Theme switching ───────────────────────────────────────────────────────

    private static void applyTheme() {
        if (themeIndex != RAINBOW_INDEX) {
            int[] p = THEMES[themeIndex];
            ACCENT = p[0]; ACCENT_DARK = p[1]; TEXT_ON = p[2];
        }
    }

    private static void cycleTheme() {
        themeIndex = (themeIndex + 1) % THEME_NAMES.length;
        applyTheme();
        themeHintUntil = System.currentTimeMillis() + 1400;
        toast("Theme: " + THEME_NAMES[themeIndex]);
        persist();
    }

    // Rainbow theme: derive the accent from the current time each frame.
    private static void updateTheme() {
        if (themeIndex != RAINBOW_INDEX) return;
        float hue = (System.currentTimeMillis() % 4000L) / 4000f;
        ACCENT      = hsvToArgb(hue, 0.75f, 1.0f);
        ACCENT_DARK = hsvToArgb(hue, 0.85f, 0.55f);
        TEXT_ON     = hsvToArgb(hue, 0.45f, 1.0f);
    }

    private static int hsvToArgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;
        int i = (int) (h * 6) % 6;
        float f = h * 6 - (float) Math.floor(h * 6);
        float p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
        switch (i) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            default: r = v; g = p; b = q; break;
        }
        return 0xFF000000 | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }

    // ── Per-frame ─────────────────────────────────────────────────────────────

    private static void onRender(Object ctx, float delta) {
        try {
            long now = System.nanoTime();
            float dt = lastFrameNanos == 0 ? 0.016f : Math.min(0.05f, (now - lastFrameNanos) / 1_000_000_000f);
            lastFrameNanos = now;

            handleInput();
            updateFullBright();
            updateZoom();
            updateTheme();
            sampleFps();

            float target = guiOpen ? 1f : 0f;
            guiAnim += (target - guiAnim) * Math.min(1f, dt * 12f);
            if (Math.abs(target - guiAnim) < 0.002f) guiAnim = target;

            leftHudY = 4;
            if (isEnabled("HUD", "Watermark"))      drawWatermark(ctx);
            else if (isEnabled("HUD", "FPS"))       drawFpsCounter(ctx);
            if (isEnabled("HUD", "FpsGraph"))       drawFpsGraph(ctx);
            if (isEnabled("HUD", "SessionInfo"))    drawSessionInfo(ctx);
            if (isEnabled("HUD", "Coordinates"))    drawCoordinates(ctx);
            if (isEnabled("HUD", "Direction"))      drawDirection(ctx);
            if (isEnabled("HUD", "Speed"))          drawSpeed(ctx);
            if (isEnabled("HUD", "Health"))         drawHealth(ctx);
            if (isEnabled("HUD", "Hunger"))         drawHunger(ctx);
            if (isEnabled("HUD", "HeldItem"))       drawHeldItem(ctx);
            if (isEnabled("HUD", "Ping"))           drawPing(ctx);
            if (isEnabled("HUD", "ServerIP"))       drawServerIp(ctx);
            if (isEnabled("HUD", "GameTime"))       drawGameTime(ctx);
            if (isEnabled("HUD", "Clock"))          drawClock(ctx);
            if (isEnabled("HUD", "Memory"))         drawMemory(ctx);
            if (isEnabled("HUD", "CPS"))            drawCps(ctx);
            if (isEnabled("HUD", "ArmorStatus"))    drawArmorStatus(ctx);
            if (isEnabled("HUD", "Keystrokes"))     drawKeystrokes(ctx);
            if (isEnabled("HUD", "ModuleList"))     drawModuleList(ctx);
            drawToasts(ctx);
            if (guiAnim > 0.01f) drawGui(ctx, guiAnim);
        } catch (Throwable t) {
            // Never crash the render thread.
        }
    }

    // ── Functional modules ───────────────────────────────────────────────────

    private static void updateFullBright() {
        boolean on = isEnabled("Render", "FullBright");
        if (on && !prevFullBright) {
            Double g = McReflect.getGamma();
            if (g != null) { savedGamma = g; McReflect.setGamma(1.0); }
        } else if (!on && prevFullBright && savedGamma != null) {
            McReflect.setGamma(savedGamma);
            savedGamma = null;
        }
        prevFullBright = on;
    }

    private static void updateZoom() {
        boolean holding = isEnabled("Render", "Zoom") && McReflect.keyDown(McReflect.KEY_C);
        if (holding && savedFov == null) {
            Double f = McReflect.getFov();
            if (f != null) { savedFov = f; McReflect.setFov(Math.max(10, f / 4)); }
        } else if (!holding && savedFov != null) {
            McReflect.setFov(savedFov);
            savedFov = null;
        }
    }

    private static void updateCps() {
        boolean down = McReflect.mouseDown(McReflect.MOUSE_LEFT);
        long now = System.currentTimeMillis();
        if (down && !prevMouseLeft) leftClickTimes.add(now);
        prevMouseLeft = down;
        leftClickTimes.removeIf(t -> now - t > 1000);
        cps = leftClickTimes.size();
    }

    // ── Input (rising-edge detection on GLFW key state) ───────────────────────

    private static void handleInput() {
        if (pressed(McReflect.KEY_RIGHT_SHIFT)) {
            guiOpen = !guiOpen;
            System.out.println("[Quark Client] Menu " + (guiOpen ? "opened" : "closed"));
        }
        if (!guiOpen) return;

        if (pressed(McReflect.KEY_ESCAPE)) { guiOpen = false; return; }

        if (pressed(McReflect.KEY_RIGHT)) { catIndex = (catIndex + 1) % CAT_NAMES.size(); modIndex = 0; }
        if (pressed(McReflect.KEY_LEFT))  { catIndex = (catIndex - 1 + CAT_NAMES.size()) % CAT_NAMES.size(); modIndex = 0; }

        int size = current().size();
        if (pressed(McReflect.KEY_DOWN)) modIndex = (modIndex + 1) % size;
        if (pressed(McReflect.KEY_UP))   modIndex = (modIndex - 1 + size) % size;

        if (pressed(McReflect.KEY_RIGHT_BRACKET)) adjustScale(SCALE_STEP);
        if (pressed(McReflect.KEY_LEFT_BRACKET))  adjustScale(-SCALE_STEP);

        if (pressed(McReflect.KEY_T)) cycleTheme();

        if (pressed(McReflect.KEY_ENTER)) {
            Module m = current().get(modIndex);
            m.enabled = !m.enabled;
            System.out.println("[Quark Client] " + m.name + " -> " + (m.enabled ? "ON" : "OFF"));
            toast(m.name + (m.enabled ? " enabled" : " disabled"));
            persist();
        }
    }

    private static void adjustScale(float delta) {
        float old = guiScale;
        guiScale = clampScale(guiScale + delta);
        if (guiScale != old) {
            scaleHintUntil = System.currentTimeMillis() + 1200;
            persist();
        }
    }

    private static float clampScale(float v) {
        v = Math.round(v / SCALE_STEP) * SCALE_STEP;
        return v < SCALE_MIN ? SCALE_MIN : (v > SCALE_MAX ? SCALE_MAX : v);
    }

    /** True on the frame a key transitions from up to down. */
    private static boolean pressed(int key) {
        boolean down = McReflect.keyDown(key);
        boolean prev = prevKey.getOrDefault(key, false);
        prevKey.put(key, down);
        return down && !prev;
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private static int nextLeftSlot(int height) {
        int y = leftHudY;
        leftHudY += height + 2;
        return y;
    }

    private static void leftLabel(Object ctx, String s, int color) {
        int x = 4, y = nextLeftSlot(12);
        McReflect.fill(ctx, x, y, x + McReflect.textWidth(s) + 8, y + 12, PANEL);
        McReflect.fill(ctx, x, y, x + 2, y + 12, ACCENT);
        McReflect.text(ctx, s, x + 6, y + 2, color);
    }

    private static void drawWatermark(Object ctx) {
        String label = "QUARK";
        String fps = fpsString();
        int x = 4, y = nextLeftSlot(14);
        int w = McReflect.textWidth(label) + (fps.isEmpty() ? 0 : McReflect.textWidth(fps) + 8) + 12;
        McReflect.fill(ctx, x, y, x + w, y + 14, PANEL);
        float pulse = 0.6f + 0.4f * (float) Math.sin(System.currentTimeMillis() / 600.0);
        McReflect.fill(ctx, x, y, x + 2, y + 14, withAlpha(ACCENT, pulse));
        McReflect.text(ctx, label, x + 6, y + 3, ACCENT);
        if (!fps.isEmpty()) McReflect.text(ctx, fps, x + 6 + McReflect.textWidth(label) + 8, y + 3, TEXT_DIM);
    }

    private static void drawFpsCounter(Object ctx) {
        String fps = fpsString();
        if (fps.isEmpty()) return;
        leftLabel(ctx, fps, ACCENT);
    }

    // Push one FPS sample at most ~5x/sec into the rolling history buffer.
    private static void sampleFps() {
        if (!isEnabled("HUD", "FpsGraph")) return;
        long now = System.currentTimeMillis();
        if (now - lastFpsSample < 200) return;
        lastFpsSample = now;
        Integer fps = McReflect.fps();
        if (fps == null) return;
        if (fpsHistoryLen < fpsHistory.length) {
            fpsHistory[fpsHistoryLen++] = fps;
        } else {
            System.arraycopy(fpsHistory, 1, fpsHistory, 0, fpsHistory.length - 1);
            fpsHistory[fpsHistory.length - 1] = fps;
        }
    }

    private static void drawFpsGraph(Object ctx) {
        if (fpsHistoryLen < 2) return;
        int w = 80, h = 26, x = 4, y = nextLeftSlot(h);
        McReflect.fill(ctx, x, y, x + w, y + h, PANEL);
        McReflect.fill(ctx, x, y, x + 2, y + h, ACCENT);

        int max = 1;
        for (int i = 0; i < fpsHistoryLen; i++) max = Math.max(max, fpsHistory[i]);

        int gx = x + 5, gy = y + 3, gw = w - 9, gh = h - 12;
        int prevX = -1, prevY = -1;
        for (int i = 0; i < fpsHistoryLen; i++) {
            int px = gx + (int) ((gw) * (i / (float) (fpsHistory.length - 1)));
            int py = gy + gh - (int) (gh * (fpsHistory[i] / (float) max));
            if (prevX >= 0) {
                // Draw a thin line segment between successive samples.
                int steps = Math.max(1, Math.abs(px - prevX));
                for (int s = 0; s <= steps; s++) {
                    int lx = prevX + (px - prevX) * s / steps;
                    int ly = prevY + (py - prevY) * s / steps;
                    McReflect.fill(ctx, lx, ly, lx + 1, ly + 1, ACCENT);
                }
            }
            prevX = px; prevY = py;
        }
        McReflect.text(ctx, fpsHistory[fpsHistoryLen - 1] + " fps", x + 5, y + h - 9, TEXT_DIM);
    }

    private static void drawCoordinates(Object ctx) {
        double[] pos = McReflect.playerPos();
        if (pos == null) return;
        leftLabel(ctx, String.format("XYZ %.1f, %.1f, %.1f", pos[0], pos[1], pos[2]), TEXT);
    }

    private static void drawDirection(Object ctx) {
        Float yaw = McReflect.getYaw();
        if (yaw == null) return;
        leftLabel(ctx, "Facing " + cardinal(yaw) + " (" + Math.round(((yaw % 360) + 360) % 360) + "°)", TEXT);
    }

    private static void drawPing(Object ctx) {
        Integer ping = McReflect.getPing();
        if (ping == null) return;
        int color = ping < 80 ? BAR_GOOD : (ping < 160 ? BAR_WARN : BAR_BAD);
        leftLabel(ctx, "Ping " + ping + " ms", color);
    }

    private static void drawClock(Object ctx) {
        leftLabel(ctx, LocalTime.now().format(CLOCK_FMT), TEXT_DIM);
    }

    private static void drawSessionInfo(Object ctx) {
        String user = McReflect.getUsername();
        if (user == null || user.isEmpty()) return;
        leftLabel(ctx, user, TEXT);
    }

    private static void drawHealth(Object ctx) {
        Float hp = McReflect.getHealth();
        if (hp == null) return;
        Float max = McReflect.getMaxHealth();
        int color = hp > 12 ? BAR_GOOD : (hp > 6 ? BAR_WARN : BAR_BAD);
        String s = max != null && max > 0
                ? String.format("HP %.1f / %.0f", hp, max)
                : String.format("HP %.1f", hp);
        leftLabel(ctx, s, color);
    }

    private static void drawHunger(Object ctx) {
        Integer food = McReflect.getFood();
        if (food == null) return;
        int color = food > 12 ? BAR_GOOD : (food > 6 ? BAR_WARN : BAR_BAD);
        leftLabel(ctx, "Food " + food + " / 20", color);
    }

    private static void drawSpeed(Object ctx) {
        double[] pos = McReflect.playerPos();
        long now = System.nanoTime();
        if (pos != null) {
            if (lastSpeedPos != null && lastSpeedNanos != 0L) {
                double dt = (now - lastSpeedNanos) / 1_000_000_000.0;
                if (dt > 0.0005) {
                    double dx = pos[0] - lastSpeedPos[0];
                    double dz = pos[2] - lastSpeedPos[2];
                    double inst = Math.sqrt(dx * dx + dz * dz) / dt;
                    // Smooth so the readout doesn't flicker frame-to-frame.
                    bps += (inst - bps) * 0.25;
                }
            }
            lastSpeedPos = pos;
            lastSpeedNanos = now;
        }
        leftLabel(ctx, String.format("Speed %.2f b/s", bps), TEXT);
    }

    private static void drawHeldItem(Object ctx) {
        String item = McReflect.getHeldItem();
        if (item == null) return;
        leftLabel(ctx, item, TEXT);
    }

    private static void drawServerIp(Object ctx) {
        String ip = McReflect.getServerAddress();
        if (ip == null || ip.isEmpty()) return;
        leftLabel(ctx, ip, TEXT_DIM);
    }

    private static void drawGameTime(Object ctx) {
        Long time = McReflect.getTimeOfDay();
        if (time == null) return;
        long day = Math.floorDiv(time, 24000L) + 1;
        long tod = Math.floorMod(time, 24000L);
        // Minecraft tick 0 == 06:00, 6000 == 12:00, 18000 == 00:00.
        long totalMin = Math.floorMod((tod * 24 * 60) / 24000 + 6 * 60, 24 * 60);
        leftLabel(ctx, String.format("Day %d  %02d:%02d", day, totalMin / 60, totalMin % 60), TEXT);
    }

    private static void drawMemory(Object ctx) {
        Runtime rt = Runtime.getRuntime();
        long max  = rt.maxMemory();
        long used = rt.totalMemory() - rt.freeMemory();
        int pct = max > 0 ? (int) Math.round(100.0 * used / max) : 0;
        int color = pct < 70 ? BAR_GOOD : (pct < 90 ? BAR_WARN : BAR_BAD);
        leftLabel(ctx, String.format("Mem %d%% (%.1f/%.1f GB)",
                pct, used / 1.073741824E9, max / 1.073741824E9), color);
    }

    private static void drawCps(Object ctx) {
        updateCps();
        leftLabel(ctx, cps + " CPS", TEXT);
    }

    private static void drawArmorStatus(Object ctx) {
        List<String> armor = McReflect.getArmorInfo();
        if (armor == null || armor.isEmpty()) return;
        for (String piece : armor) {
            // Color the row by the durability percentage if present.
            int color = TEXT;
            int pctIdx = piece.lastIndexOf(' ');
            if (pctIdx > 0 && piece.endsWith("%")) {
                try {
                    int pct = Integer.parseInt(piece.substring(pctIdx + 1, piece.length() - 1));
                    color = pct > 50 ? BAR_GOOD : (pct > 20 ? BAR_WARN : BAR_BAD);
                } catch (NumberFormatException ignored) {}
            }
            leftLabel(ctx, piece, color);
        }
    }

    private static void drawKeystrokes(Object ctx) {
        updateCps();
        int sw = McReflect.screenWidth(ctx);
        int sh = McReflect.screenHeight(ctx);
        int cell = 18, gap = 2;
        int cx = sw / 2;
        int baseY = sh - 84;

        String cpsText = cps + " CPS";
        McReflect.text(ctx, cpsText, cx - McReflect.textWidth(cpsText) / 2, baseY - 12, TEXT_DIM);

        drawKey(ctx, cx - cell - gap, baseY, cell, "W", McReflect.keyDown(87));
        drawKey(ctx, cx - cell - gap, baseY + cell + gap, cell, "A", McReflect.keyDown(65));
        drawKey(ctx, cx, baseY + cell + gap, cell, "S", McReflect.keyDown(83));
        drawKey(ctx, cx + cell + gap, baseY + cell + gap, cell, "D", McReflect.keyDown(68));

        int mx = cx + 2 * (cell + gap) + 8;
        drawKey(ctx, mx, baseY, cell, "L", McReflect.mouseDown(McReflect.MOUSE_LEFT));
        drawKey(ctx, mx, baseY + cell + gap, cell, "R", McReflect.mouseDown(McReflect.MOUSE_RIGHT));
    }

    private static void drawKey(Object ctx, int x, int y, int size, String label, boolean down) {
        McReflect.fill(ctx, x, y, x + size, y + size, down ? ACCENT_DARK : PANEL);
        McReflect.outline(ctx, x, y, x + size, y + size, down ? ACCENT : ROW);
        McReflect.text(ctx, label, x + size / 2 - McReflect.textWidth(label) / 2, y + size / 2 - 4, down ? TEXT : TEXT_DIM);
    }

    private static void drawModuleList(Object ctx) {
        List<String> active = new ArrayList<>();
        for (List<Module> mods : CATEGORIES.values())
            for (Module m : mods)
                if (m.enabled && !SELF_RENDERING.contains(m.name)) active.add(m.name);
        active.sort((a, b) -> McReflect.textWidth(b) - McReflect.textWidth(a));

        int sw = McReflect.screenWidth(ctx);
        int y = 4;
        for (String name : active) {
            int w = McReflect.textWidth(name);
            int x = sw - w - 6;
            McReflect.fill(ctx, x - 2, y, sw, y + 11, PANEL);
            McReflect.fill(ctx, sw - 1, y, sw, y + 11, ACCENT);
            McReflect.text(ctx, name, x, y + 2, TEXT);
            y += 12;
        }
    }

    private static void drawToasts(Object ctx) {
        if (TOASTS.isEmpty()) return;
        long now = System.currentTimeMillis();
        TOASTS.removeIf(t -> now - t.start > TOAST_LIFETIME_MS);

        int sh = McReflect.screenHeight(ctx);
        int y = sh - 24;
        for (int i = TOASTS.size() - 1; i >= 0; i--) {
            Toast t = TOASTS.get(i);
            long age = now - t.start;
            float alpha = age > TOAST_LIFETIME_MS - TOAST_FADE_MS
                    ? (TOAST_LIFETIME_MS - age) / (float) TOAST_FADE_MS : 1f;
            alpha = clamp01(alpha);
            int w = McReflect.textWidth(t.text) + 16;
            int x = 6;
            McReflect.fill(ctx, x, y, x + w, y + 14, withAlpha(PANEL, alpha));
            McReflect.fill(ctx, x, y, x + 2, y + 14, withAlpha(ACCENT, alpha));
            McReflect.text(ctx, t.text, x + 8, y + 3, withAlpha(TEXT, alpha));
            y -= 16;
        }
    }

    private static void toast(String text) {
        if (!isEnabled("Misc", "Notifications")) return;
        TOASTS.add(new Toast(text));
        while (TOASTS.size() > 5) TOASTS.remove(0);
    }

    // ── ClickGUI (sidebar layout, scalable) ───────────────────────────────────

    private static void drawGui(Object ctx, float anim) {
        int rawW = McReflect.screenWidth(ctx);
        int rawH = McReflect.screenHeight(ctx);

        // Full-screen dim is drawn unscaled so it always covers the whole screen.
        McReflect.fill(ctx, 0, 0, rawW, rawH, withAlpha(0x99000000, anim));

        // Apply UI scale around the screen centre, then lay out the panel in scaled space.
        boolean scaled = guiScale != 1.0f && McReflect.pushScale(ctx, guiScale, rawW / 2f, rawH / 2f);
        try {
            int sw = rawW, sh = rawH;

            int panelW = 380, panelH = 268;
            int px = (sw - panelW) / 2;
            int py = (sh - panelH) / 2 + (int) ((1f - anim) * 18f);

            McReflect.fill(ctx, px, py, px + panelW, py + panelH, withAlpha(PANEL, anim));
            McReflect.outline(ctx, px, py, px + panelW, py + panelH, withAlpha(ACCENT_DARK, anim));

            int headerH = 22;
            McReflect.fill(ctx, px, py, px + panelW, py + headerH, withAlpha(HEADER_BG, anim));
            McReflect.text(ctx, "QUARK", px + 10, py + 7, withAlpha(ACCENT, anim));
            McReflect.text(ctx, "CLIENT", px + 10 + McReflect.textWidth("QUARK") + 6, py + 7, withAlpha(TEXT_DIM, anim));
            String fps = fpsString();
            if (!fps.isEmpty())
                McReflect.text(ctx, fps, px + panelW - McReflect.textWidth(fps) - 10, py + 7, withAlpha(TEXT_DIM, anim));
            McReflect.fill(ctx, px, py + headerH, px + panelW, py + headerH + 1, withAlpha(ACCENT_DARK, anim));

            int sidebarW = 108;
            int footerH = 36;
            int bodyY = py + headerH + 1;
            int bodyH = panelH - headerH - 1 - footerH;

            int catY = bodyY + 4;
            int catRowH = 18;
            for (int i = 0; i < CAT_NAMES.size(); i++) {
                String c = CAT_NAMES.get(i);
                boolean sel = i == catIndex;
                if (sel) {
                    McReflect.fill(ctx, px + 2, catY, px + 4, catY + catRowH, withAlpha(ACCENT, anim));
                    McReflect.fill(ctx, px + 4, catY, px + sidebarW, catY + catRowH, withAlpha(ROW_SEL, anim));
                }
                McReflect.text(ctx, c, px + 10, catY + 5, withAlpha(sel ? TEXT : TEXT_DIM, anim));
                int count = countEnabled(c);
                if (count > 0) {
                    String badge = String.valueOf(count);
                    McReflect.text(ctx, badge, px + sidebarW - McReflect.textWidth(badge) - 6, catY + 5,
                            withAlpha(sel ? ACCENT : TEXT_DIM, anim));
                }
                catY += catRowH;
            }

            McReflect.fill(ctx, px + sidebarW, bodyY, px + sidebarW + 1, bodyY + bodyH, withAlpha(ACCENT_DARK, anim));

            List<Module> mods = current();
            int colX = px + sidebarW + 6;
            int colW = panelW - sidebarW - 10;
            int rowH = 16;
            int maxRows = bodyH / rowH;
            int start = Math.max(0, Math.min(modIndex - maxRows / 2, Math.max(0, mods.size() - maxRows)));

            for (int i = start; i < mods.size() && i < start + maxRows; i++) {
                Module m = mods.get(i);
                int ry = bodyY + (i - start) * rowH;
                boolean sel = i == modIndex;
                McReflect.fill(ctx, colX, ry + 1, colX + colW, ry + rowH - 1, withAlpha(sel ? ROW_SEL : ROW, anim));
                if (sel) McReflect.fill(ctx, colX, ry + 1, colX + 2, ry + rowH - 1, withAlpha(ACCENT, anim));
                McReflect.text(ctx, m.name, colX + 8, ry + 4, withAlpha(m.enabled ? TEXT_ON : TEXT, anim));
                String state = m.enabled ? "ON" : "OFF";
                McReflect.text(ctx, state, colX + colW - McReflect.textWidth(state) - 8, ry + 4,
                        withAlpha(m.enabled ? ACCENT : TEXT_DIM, anim));
            }

            int footerY = py + panelH - footerH;
            McReflect.fill(ctx, px, footerY, px + panelW, py + panelH, withAlpha(HEADER_BG, anim));
            Module selMod = mods.get(modIndex);
            McReflect.text(ctx, selMod.name + " — " + selMod.description, px + 10, footerY + 5, withAlpha(TEXT_DIM, anim));

            // Show the live scale/theme when adjusting, otherwise the controls hint.
            long nowMs = System.currentTimeMillis();
            if (nowMs < scaleHintUntil) {
                String s = "UI scale " + Math.round(guiScale * 100) + "%";
                McReflect.text(ctx, s, px + 10, footerY + 18, withAlpha(ACCENT, anim));
                String range = "[ - ]  +";
                McReflect.text(ctx, range, px + panelW - McReflect.textWidth(range) - 10, footerY + 18, withAlpha(TEXT_DIM, anim));
            } else if (nowMs < themeHintUntil) {
                String s = "Theme: " + THEME_NAMES[themeIndex];
                McReflect.text(ctx, s, px + 10, footerY + 18, withAlpha(ACCENT, anim));
            } else {
                String hint = "←→ Cat   ↑↓ Mod   Enter Toggle   [ ] Scale   T Theme";
                McReflect.text(ctx, hint, px + 10, footerY + 18, withAlpha(TEXT_DIM, anim));
            }
        } finally {
            if (scaled) McReflect.popMatrix(ctx);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String cardinal(float yaw) {
        // Minecraft yaw: 0=South, 90=West, 180=North, 270=East.
        float a = ((yaw % 360) + 360) % 360;
        String[] dirs = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return dirs[Math.round(a / 45f) % 8];
    }

    private static boolean isEnabled(String cat, String name) {
        List<Module> mods = CATEGORIES.get(cat);
        if (mods == null) return false;
        for (Module m : mods) if (m.name.equals(name)) return m.enabled;
        return false;
    }

    private static int countEnabled(String cat) {
        int n = 0;
        for (Module m : CATEGORIES.get(cat)) if (m.enabled) n++;
        return n;
    }

    private static String fpsString() {
        Integer fps = McReflect.fps();
        return fps == null ? "" : (fps + " FPS");
    }

    private static int withAlpha(int argb, float alpha) {
        int a = (int) (((argb >>> 24) & 0xFF) * clamp01(alpha));
        return (a << 24) | (argb & 0x00FFFFFF);
    }

    private static float clamp01(float v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
}
