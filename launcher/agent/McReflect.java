package cc.quark.agent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Reflection helpers for talking to a live Minecraft from inside the agent,
 * without compile-time access to Minecraft classes.
 *
 * Every lookup tries multiple mapping variants in order:
 *   Yarn (dev) → Intermediary (Fabric production) → Mojmap (Forge/NeoForge).
 * Intermediary names target Minecraft 1.21.x, which is what the launcher
 * advertises as its primary runtime.
 *
 * All resolved members are cached. Failures are logged once and degrade
 * gracefully (the caller no-ops) so a wrong name never crashes the game.
 */
public final class McReflect {

    private McReflect() {}

    // ── Cached members ────────────────────────────────────────────────────────
    private static Method  fillM;          // DrawContext.fill(int,int,int,int,int)
    private static Method  drawTextM;       // DrawContext.drawText(TextRenderer,String,int,int,int,boolean)
    private static Method  scaledWidthM;    // DrawContext.getScaledWindowWidth()
    private static Method  scaledHeightM;   // DrawContext.getScaledWindowHeight()
    private static Field   textRendererF;   // MinecraftClient.textRenderer
    private static Method  getWindowM;      // MinecraftClient.getWindow()
    private static Method  getHandleM;      // Window.getHandle()
    private static Method  glfwGetKeyM;     // GLFW.glfwGetKey(long,int)
    private static Method  textWidthM;      // TextRenderer.getWidth(String)

    private static Method  getFpsM;         // MinecraftClient.getCurrentFps()
    private static boolean getFpsResolved = false;

    private static Object  mcInstance;
    private static long    windowHandle = 0L;
    private static boolean windowResolved = false;

    // ── Bootstrap ─────────────────────────────────────────────────────────────

    public static void bind(Object mc) {
        mcInstance = mc;
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    /** Filled rectangle. color is ARGB. */
    public static void fill(Object ctx, int x1, int y1, int x2, int y2, int color) {
        try {
            if (fillM == null) {
                fillM = findMethod(ctx.getClass(),
                        new String[]{"fill", "method_25294", "m_280509_"},
                        int.class, int.class, int.class, int.class, int.class);
            }
            if (fillM != null) fillM.invoke(ctx, x1, y1, x2, y2, color);
        } catch (Throwable ignored) {}
    }

    /** Outlined rectangle (1px border). */
    public static void outline(Object ctx, int x1, int y1, int x2, int y2, int color) {
        fill(ctx, x1, y1, x2, y1 + 1, color);          // top
        fill(ctx, x1, y2 - 1, x2, y2, color);          // bottom
        fill(ctx, x1, y1, x1 + 1, y2, color);          // left
        fill(ctx, x2 - 1, y1, x2, y2, color);          // right
    }

    /** Draw text with shadow. Returns silently on failure. */
    public static void text(Object ctx, String s, int x, int y, int color) {
        try {
            Object tr = textRenderer();
            if (tr == null) return;
            if (drawTextM == null) {
                drawTextM = findMethod(ctx.getClass(),
                        new String[]{"drawText", "method_25303", "m_280488_"},
                        tr.getClass(), String.class, int.class, int.class, int.class, boolean.class);
                if (drawTextM == null) {
                    // Some mappings type the first arg as a supertype; match by shape.
                    drawTextM = findMethodByShape(ctx.getClass(),
                            new String[]{"drawText", "method_25303", "m_280488_"},
                            6, int.class, String.class, int.class, int.class, int.class, boolean.class);
                }
            }
            if (drawTextM != null) drawTextM.invoke(ctx, tr, s, x, y, color, true);
        } catch (Throwable ignored) {}
    }

    public static int textWidth(String s) {
        try {
            Object tr = textRenderer();
            if (tr == null) return s.length() * 6;
            if (textWidthM == null) {
                textWidthM = findMethod(tr.getClass(),
                        new String[]{"getWidth", "method_1727", "m_92895_"}, String.class);
            }
            if (textWidthM != null) return (int) textWidthM.invoke(tr, s);
        } catch (Throwable ignored) {}
        return s.length() * 6;
    }

    public static int screenWidth(Object ctx) {
        try {
            if (scaledWidthM == null) {
                scaledWidthM = findMethod(ctx.getClass(),
                        new String[]{"getScaledWindowWidth", "method_51421", "m_280182_"});
            }
            if (scaledWidthM != null) return (int) scaledWidthM.invoke(ctx);
        } catch (Throwable ignored) {}
        return 1920;
    }

    public static int screenHeight(Object ctx) {
        try {
            if (scaledHeightM == null) {
                scaledHeightM = findMethod(ctx.getClass(),
                        new String[]{"getScaledWindowHeight", "method_51443", "m_280183_"});
            }
            if (scaledHeightM != null) return (int) scaledHeightM.invoke(ctx);
        } catch (Throwable ignored) {}
        return 1080;
    }

    // ── Input (GLFW polling, render-thread safe) ──────────────────────────────

    public static final int KEY_RIGHT_SHIFT = 344;
    public static final int KEY_UP          = 265;
    public static final int KEY_DOWN        = 264;
    public static final int KEY_LEFT        = 263;
    public static final int KEY_RIGHT       = 262;
    public static final int KEY_ENTER       = 257;
    public static final int KEY_ESCAPE      = 256;
    public static final int KEY_C           = 67;

    public static final int MOUSE_LEFT  = 0;
    public static final int MOUSE_RIGHT = 1;

    private static Method glfwGetMouseButtonM;
    private static Class<?> glfwClass;

    /** True while the given GLFW key is physically held down. */
    public static boolean keyDown(int key) {
        try {
            long handle = window();
            if (handle == 0L) return false;
            if (glfwGetKeyM == null) {
                glfwGetKeyM = glfw().getMethod("glfwGetKey", long.class, int.class);
            }
            int state = (int) glfwGetKeyM.invoke(null, handle, key);
            return state == 1; // GLFW_PRESS
        } catch (Throwable ignored) {}
        return false;
    }

    /** True while the given GLFW mouse button (MOUSE_LEFT/MOUSE_RIGHT) is held down. */
    public static boolean mouseDown(int button) {
        try {
            long handle = window();
            if (handle == 0L) return false;
            if (glfwGetMouseButtonM == null) {
                glfwGetMouseButtonM = glfw().getMethod("glfwGetMouseButton", long.class, int.class);
            }
            int state = (int) glfwGetMouseButtonM.invoke(null, handle, button);
            return state == 1; // GLFW_PRESS
        } catch (Throwable ignored) {}
        return false;
    }

    private static Class<?> glfw() throws ClassNotFoundException {
        if (glfwClass == null) {
            glfwClass = Class.forName("org.lwjgl.glfw.GLFW", false,
                    mcInstance != null ? mcInstance.getClass().getClassLoader()
                                       : Thread.currentThread().getContextClassLoader());
        }
        return glfwClass;
    }

    /** Current FPS, or null if it can't be resolved. */
    public static Integer fps() {
        if (mcInstance == null) return null;
        try {
            if (!getFpsResolved) {
                getFpsM = findMethod(mcInstance.getClass(),
                        new String[]{"getCurrentFps", "method_47599", "m_260863_"});
                getFpsResolved = true;
            }
            if (getFpsM != null) return (Integer) getFpsM.invoke(mcInstance);
        } catch (Throwable ignored) {}
        return null;
    }

    // ── Player state ──────────────────────────────────────────────────────────

    private static Field  playerF;
    private static Method getXM, getYM, getZM;

    /** [x, y, z] of the local player, or null if unresolved. */
    public static double[] playerPos() {
        try {
            Object player = player();
            if (player == null) return null;
            if (getXM == null) getXM = findMethod(player.getClass(), new String[]{"getX", "method_23317"});
            if (getYM == null) getYM = findMethod(player.getClass(), new String[]{"getY", "method_23318"});
            if (getZM == null) getZM = findMethod(player.getClass(), new String[]{"getZ", "method_23321"});
            if (getXM != null && getYM != null && getZM != null) {
                return new double[]{
                        ((Number) getXM.invoke(player)).doubleValue(),
                        ((Number) getYM.invoke(player)).doubleValue(),
                        ((Number) getZM.invoke(player)).doubleValue()
                };
            }
            // Old Forge MCP (pre-1.13) exposed raw fields instead of accessor methods.
            Field xf = findField(player.getClass(), "posX");
            Field yf = findField(player.getClass(), "posY");
            Field zf = findField(player.getClass(), "posZ");
            if (xf != null && yf != null && zf != null) {
                return new double[]{ xf.getDouble(player), yf.getDouble(player), zf.getDouble(player) };
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static Object player() {
        if (mcInstance == null) return null;
        try {
            if (playerF == null) playerF = findField(mcInstance.getClass(), "player", "field_1724", "f_91074_");
            return playerF != null ? playerF.get(mcInstance) : null;
        } catch (Throwable ignored) { return null; }
    }

    // ── Game options (best effort — names vary most across versions here) ─────

    private static Field gameOptionsF;
    private static final Map<String, Field> optionFieldCache = new HashMap<>();

    /** Brightness slider value (0.0-1.0 in vanilla), or null if unresolved. */
    public static Double getGamma() { return getOptionValue("gamma"); }
    public static void   setGamma(double v) { setOptionValue("gamma", v); }

    /** Field of view value, or null if unresolved. */
    public static Double getFov() { return getOptionValue("fov"); }
    public static void   setFov(double v) { setOptionValue("fov", v); }

    private static Object gameOptions() {
        if (mcInstance == null) return null;
        try {
            if (gameOptionsF == null) gameOptionsF = findField(mcInstance.getClass(), "options", "field_1690", "f_91066_");
            return gameOptionsF != null ? gameOptionsF.get(mcInstance) : null;
        } catch (Throwable ignored) { return null; }
    }

    /** Reads a SimpleOption<Double>/OptionInstance<Double>-style settings field by name. */
    private static Double getOptionValue(String fieldName) {
        try {
            Object options = gameOptions();
            if (options == null) return null;
            Field f = optionFieldCache.computeIfAbsent(fieldName, n -> findField(options.getClass(), n));
            if (f == null) return null;
            Object opt = f.get(options);
            if (opt == null) return null;
            for (String m : new String[]{"getValue", "get", "value"}) {
                try {
                    Object v = opt.getClass().getMethod(m).invoke(opt);
                    if (v instanceof Number) return ((Number) v).doubleValue();
                } catch (NoSuchMethodException ignored) {}
            }
        } catch (Throwable ignored) {}
        return null;
    }

    private static void setOptionValue(String fieldName, double value) {
        try {
            Object options = gameOptions();
            if (options == null) return;
            Field f = optionFieldCache.computeIfAbsent(fieldName, n -> findField(options.getClass(), n));
            if (f == null) return;
            Object opt = f.get(options);
            if (opt == null) return;
            Method setter = findMethodByShape(opt.getClass(), new String[]{"setValue", "set"}, 1, Object.class);
            if (setter == null) return;
            try { setter.invoke(opt, value); return; } catch (Throwable ignored) {}
            try { setter.invoke(opt, (int) Math.round(value)); } catch (Throwable ignored) {}
        } catch (Throwable ignored) {}
    }

    // ── Internal resolution ───────────────────────────────────────────────────

    private static Object textRenderer() {
        if (mcInstance == null) return null;
        try {
            if (textRendererF == null) {
                textRendererF = findField(mcInstance.getClass(),
                        "textRenderer", "field_1772", "f_91062_");
                if (textRendererF == null) {
                    // Fallback: first field whose type looks like a TextRenderer
                    for (Field f : mcInstance.getClass().getDeclaredFields()) {
                        String t = f.getType().getName().toLowerCase();
                        if (t.contains("textrenderer") || t.equals("net.minecraft.class_327")) {
                            f.setAccessible(true); textRendererF = f; break;
                        }
                    }
                }
            }
            return textRendererF != null ? textRendererF.get(mcInstance) : null;
        } catch (Throwable ignored) {}
        return null;
    }

    private static long window() {
        if (windowResolved) return windowHandle;
        try {
            if (mcInstance != null) {
                if (getWindowM == null) {
                    getWindowM = findMethod(mcInstance.getClass(),
                            new String[]{"getWindow", "method_22683", "m_91268_"});
                }
                Object win = getWindowM != null ? getWindowM.invoke(mcInstance) : null;
                if (win != null) {
                    if (getHandleM == null) {
                        getHandleM = findMethod(win.getClass(),
                                new String[]{"getHandle", "method_4490", "m_85439_"});
                    }
                    if (getHandleM != null) windowHandle = (long) getHandleM.invoke(win);
                }
            }
        } catch (Throwable ignored) {}
        windowResolved = windowHandle != 0L;   // retry next frame until found
        return windowHandle;
    }

    static Method findMethod(Class<?> owner, String[] names, Class<?>... params) {
        for (String n : names) {
            try {
                Method m = owner.getMethod(n, params);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {}
            try {
                Method m = owner.getDeclaredMethod(n, params);
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException ignored) {}
        }
        return null;
    }

    /** Match by name + parameter-count + primitive-shape (ignores exact object types). */
    static Method findMethodByShape(Class<?> owner, String[] names, int paramCount, Class<?>... shape) {
        for (Method m : allMethods(owner)) {
            if (!nameMatches(m.getName(), names)) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length != paramCount) continue;
            boolean ok = true;
            for (int i = 0; i < p.length && i < shape.length; i++) {
                if (shape[i].isPrimitive() && p[i] != shape[i]) { ok = false; break; }
            }
            if (ok) { m.setAccessible(true); return m; }
        }
        return null;
    }

    private static boolean nameMatches(String name, String[] names) {
        for (String n : names) if (n.equals(name)) return true;
        return false;
    }

    private static java.util.List<Method> allMethods(Class<?> c) {
        java.util.List<Method> out = new java.util.ArrayList<>();
        while (c != null && c != Object.class) {
            for (Method m : c.getDeclaredMethods()) out.add(m);
            c = c.getSuperclass();
        }
        return out;
    }

    private static Field findField(Class<?> owner, String... names) {
        Class<?> c = owner;
        while (c != null && c != Object.class) {
            for (String n : names) {
                try {
                    Field f = c.getDeclaredField(n);
                    f.setAccessible(true);
                    return f;
                } catch (NoSuchFieldException ignored) {}
            }
            c = c.getSuperclass();
        }
        return null;
    }
}
