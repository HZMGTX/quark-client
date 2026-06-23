package cc.quark.agent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Self-contained Quark client that runs purely from the injected agent —
 * no Fabric mod, no mods folder. Provides a keyboard-driven ClickGUI plus
 * a HUD (watermark + module list), rendered with McReflect against the live
 * Minecraft DrawContext.
 *
 * Right-Shift toggles the menu. Arrow keys navigate, Enter toggles a module.
 *
 * Modules are Lunar/Feather-style quality-of-life and visual features. Combat
 * and movement entries are presentation toggles only — the agent does not ship
 * functional combat automation.
 */
public final class StandaloneClient {

    private StandaloneClient() {}

    // ── Theme (ARGB) ──────────────────────────────────────────────────────────
    private static final int ACCENT      = 0xFF8A5CF6;
    private static final int ACCENT_DARK = 0xFF6D28D9;
    private static final int BG          = 0xE6101019;
    private static final int PANEL       = 0xF01A1A28;
    private static final int HEADER      = 0xFF8A5CF6;
    private static final int ROW         = 0xFF14141F;
    private static final int ROW_SEL     = 0xFF24243A;
    private static final int TEXT        = 0xFFFFFFFF;
    private static final int TEXT_DIM    = 0xFF9090A0;
    private static final int TEXT_ON     = 0xFFB794F6;

    // ── Module model ──────────────────────────────────────────────────────────
    public static final class Module {
        public final String name;
        public final String description;
        public boolean enabled;
        Module(String name, String description) { this.name = name; this.description = description; }
    }

    private static final Map<String, List<Module>> CATEGORIES = new LinkedHashMap<>();
    private static final List<String> CAT_NAMES = new ArrayList<>();

    private static boolean guiOpen = false;
    private static int catIndex = 0;
    private static int modIndex = 0;
    private static boolean built = false;

    // edge-detection state
    private static final Map<Integer, Boolean> prevKey = new LinkedHashMap<>();

    // ── Init ──────────────────────────────────────────────────────────────────

    public static void init(Object mc) {
        if (built) return;
        built = true;
        McReflect.bind(mc);
        buildModules();

        // Render every HUD frame.
        MinecraftHook.addRender2DListener(StandaloneClient::onRender);
        System.out.println("[Quark Client] Standalone client ready — press Right-Shift in game to open the menu.");
    }

    private static void buildModules() {
        add("Combat",   "KillAura",      "Visual toggle");
        add("Combat",   "Criticals",     "Visual toggle");
        add("Combat",   "AutoClicker",   "Visual toggle");
        add("Combat",   "Reach",         "Visual toggle");
        add("Combat",   "Velocity",      "Visual toggle");

        add("Movement", "Sprint",        "Always sprint indicator");
        add("Movement", "ToggleSprint",  "Toggle-to-sprint");
        add("Movement", "NoSlow",        "Visual toggle");
        add("Movement", "Speed",         "Visual toggle");
        add("Movement", "Flight",        "Visual toggle");
        add("Movement", "Step",          "Visual toggle");

        add("Render",   "FullBright",    "Visual toggle");
        add("Render",   "Zoom",          "Hold to zoom");
        add("Render",   "MotionBlur",    "Visual toggle");
        add("Render",   "ViewModel",     "Visual toggle");
        add("Render",   "Ambiance",      "Visual toggle");
        add("Render",   "NoHurtCam",     "Visual toggle");

        add("HUD",      "Watermark",     "Quark logo + FPS", true);
        add("HUD",      "ModuleList",    "Active module list", true);
        add("HUD",      "FPS",           "FPS counter");
        add("HUD",      "Keystrokes",    "Visual toggle");
        add("HUD",      "ArmorStatus",   "Visual toggle");
        add("HUD",      "Coordinates",   "Visual toggle");

        add("Player",   "AutoRespawn",   "Visual toggle");
        add("Player",   "FastUse",       "Visual toggle");
        add("Player",   "FreeLook",      "Visual toggle");

        add("Misc",     "DiscordRPC",    "Visual toggle");
        add("Misc",     "FpsBoost",      "Visual toggle");
        add("Misc",     "ClickGui",      "This menu");
    }

    private static void add(String cat, String name, String desc) { add(cat, name, desc, false); }
    private static void add(String cat, String name, String desc, boolean on) {
        CATEGORIES.computeIfAbsent(cat, k -> { CAT_NAMES.add(k); return new ArrayList<>(); });
        Module m = new Module(name, desc);
        m.enabled = on;
        CATEGORIES.get(cat).add(m);
    }

    private static List<Module> current() { return CATEGORIES.get(CAT_NAMES.get(catIndex)); }

    // ── Per-frame ─────────────────────────────────────────────────────────────

    private static void onRender(Object ctx, float delta) {
        try {
            handleInput();
            if (isEnabled("HUD", "Watermark"))   drawWatermark(ctx);
            if (isEnabled("HUD", "ModuleList"))  drawModuleList(ctx);
            if (guiOpen)                          drawGui(ctx);
        } catch (Throwable t) {
            // Never crash the render thread.
        }
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

        if (pressed(McReflect.KEY_ENTER)) {
            Module m = current().get(modIndex);
            m.enabled = !m.enabled;
            System.out.println("[Quark Client] " + m.name + " -> " + (m.enabled ? "ON" : "OFF"));
        }
    }

    /** True on the frame a key transitions from up to down. */
    private static boolean pressed(int key) {
        boolean down = McReflect.keyDown(key);
        boolean prev = prevKey.getOrDefault(key, false);
        prevKey.put(key, down);
        return down && !prev;
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private static void drawWatermark(Object ctx) {
        String label = "Quark";
        String fps = fpsString();
        int x = 4, y = 4;
        McReflect.fill(ctx, x, y, x + 6 + McReflect.textWidth(label) + (fps.isEmpty() ? 0 : McReflect.textWidth(fps) + 8) + 6, y + 14, BG);
        McReflect.fill(ctx, x, y, x + 2, y + 14, ACCENT);
        McReflect.text(ctx, label, x + 6, y + 3, ACCENT);
        if (!fps.isEmpty()) McReflect.text(ctx, fps, x + 6 + McReflect.textWidth(label) + 8, y + 3, TEXT_DIM);
    }

    private static void drawModuleList(Object ctx) {
        List<String> active = new ArrayList<>();
        for (List<Module> mods : CATEGORIES.values())
            for (Module m : mods)
                if (m.enabled && !m.name.equals("Watermark") && !m.name.equals("ModuleList")) active.add(m.name);
        active.sort((a, b) -> McReflect.textWidth(b) - McReflect.textWidth(a));

        int sw = McReflect.screenWidth(ctx);
        int y = 4;
        for (String name : active) {
            int w = McReflect.textWidth(name);
            int x = sw - w - 6;
            McReflect.fill(ctx, x - 2, y, sw, y + 11, BG);
            McReflect.fill(ctx, sw - 1, y, sw, y + 11, ACCENT);
            McReflect.text(ctx, name, x, y + 2, TEXT);
            y += 12;
        }
    }

    // ── ClickGUI ──────────────────────────────────────────────────────────────

    private static void drawGui(Object ctx) {
        int sw = McReflect.screenWidth(ctx);
        int sh = McReflect.screenHeight(ctx);

        int panelW = 320;
        int panelH = 230;
        int px = (sw - panelW) / 2;
        int py = (sh - panelH) / 2;

        // dim background
        McReflect.fill(ctx, 0, 0, sw, sh, 0x70000000);

        // panel
        McReflect.fill(ctx, px, py, px + panelW, py + panelH, BG);
        McReflect.outline(ctx, px, py, px + panelW, py + panelH, ACCENT);

        // title bar
        McReflect.fill(ctx, px, py, px + panelW, py + 20, PANEL);
        McReflect.text(ctx, "QUARK  ·  Ghost Client", px + 8, py + 6, ACCENT);
        String hint = "←→ category   ↑↓ module   ⏎ toggle   RShift close";
        McReflect.text(ctx, hint, px + panelW - McReflect.textWidth(hint) - 8, py + 6, TEXT_DIM);

        // category tabs
        int tabY = py + 22;
        int tabX = px + 4;
        for (int i = 0; i < CAT_NAMES.size(); i++) {
            String c = CAT_NAMES.get(i);
            int w = McReflect.textWidth(c) + 12;
            boolean sel = i == catIndex;
            McReflect.fill(ctx, tabX, tabY, tabX + w, tabY + 16, sel ? ACCENT_DARK : ROW);
            if (sel) McReflect.fill(ctx, tabX, tabY + 15, tabX + w, tabY + 16, ACCENT);
            McReflect.text(ctx, c, tabX + 6, tabY + 4, sel ? TEXT : TEXT_DIM);
            tabX += w + 3;
        }

        // module rows
        List<Module> mods = current();
        int rowY = tabY + 22;
        int rowH = 16;
        int maxRows = (py + panelH - rowY - 6) / rowH;
        int start = Math.max(0, Math.min(modIndex - maxRows / 2, Math.max(0, mods.size() - maxRows)));

        for (int i = start; i < mods.size() && i < start + maxRows; i++) {
            Module m = mods.get(i);
            int ry = rowY + (i - start) * rowH;
            boolean sel = i == modIndex;
            McReflect.fill(ctx, px + 4, ry, px + panelW - 4, ry + rowH - 2, sel ? ROW_SEL : ROW);
            if (sel) McReflect.fill(ctx, px + 4, ry, px + 6, ry + rowH - 2, ACCENT);
            McReflect.text(ctx, m.name, px + 12, ry + 4, m.enabled ? TEXT_ON : TEXT);

            String state = m.enabled ? "ON" : "OFF";
            int stateColor = m.enabled ? ACCENT : TEXT_DIM;
            McReflect.text(ctx, state, px + panelW - McReflect.textWidth(state) - 12, ry + 4, stateColor);
        }

        // footer: selected module description
        Module selMod = mods.get(modIndex);
        McReflect.fill(ctx, px, py + panelH - 14, px + panelW, py + panelH, PANEL);
        McReflect.text(ctx, selMod.name + " — " + selMod.description, px + 8, py + panelH - 11, TEXT_DIM);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean isEnabled(String cat, String name) {
        List<Module> mods = CATEGORIES.get(cat);
        if (mods == null) return false;
        for (Module m : mods) if (m.name.equals(name)) return m.enabled;
        return false;
    }

    private static String fpsString() {
        Integer fps = McReflect.fps();
        return fps == null ? "" : (fps + " FPS");
    }
}
