package cc.quark.agent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Central dispatch hub for all injected Minecraft hooks.
 * Called by AsmPatcher-instrumented methods inside the Minecraft JVM.
 *
 * When running alongside the Fabric mod, this delegates to Quark's real
 * EventBus. When running standalone (pure injection), it uses its own
 * lightweight listener list.
 */
public class MinecraftHook {

    // ── Lightweight hook interfaces ───────────────────────────────────────────

    public interface TickListener    { void onTick(Object mc); }
    public interface Render2DListener{ void onRender2D(Object ctx, float delta); }
    public interface Render3DListener{ void onRender3D(Object renderer); }
    public interface PacketListener  { boolean onPacket(Object packet); /* false = cancel */ }
    public interface KeyListener     { void onKey(int key, int action); }

    private static final CopyOnWriteArrayList<TickListener>     TICK     = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Render2DListener> RENDER2D = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<Render3DListener> RENDER3D = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<PacketListener>   PACKET   = new CopyOnWriteArrayList<>();
    private static final CopyOnWriteArrayList<KeyListener>      KEY      = new CopyOnWriteArrayList<>();

    // Cached Quark EventBus reference (if Fabric mod is loaded)
    private static volatile Object quarkEventBus = null;
    private static volatile Method postMethod     = null;
    private static volatile Object mcInstance     = null;

    // ── Registration ─────────────────────────────────────────────────────────

    public static void addTickListener(TickListener l)         { TICK.add(l); }
    public static void addRender2DListener(Render2DListener l) { RENDER2D.add(l); }
    public static void addRender3DListener(Render3DListener l) { RENDER3D.add(l); }
    public static void addPacketListener(PacketListener l)     { PACKET.add(l); }
    public static void addKeyListener(KeyListener l)           { KEY.add(l); }

    // ── Setup ─────────────────────────────────────────────────────────────────

    public static void setup(Object mc, ClassLoader cl) {
        mcInstance = mc;
        System.out.println("[Quark Hook] MinecraftHook active. MC=" + mc.getClass().getSimpleName());

        // Try to wire into Quark's real EventBus (if Fabric mod is already loaded)
        tryWireEventBus(cl);
    }

    private static void tryWireEventBus(ClassLoader cl) {
        try {
            Class<?> quark = Class.forName("cc.quark.Quark", false, cl);
            Method gi = quark.getDeclaredMethod("getInstance");
            gi.setAccessible(true);
            Object quarkInst = gi.invoke(null);
            if (quarkInst == null) return;

            Method geb = quark.getDeclaredMethod("getEventBus");
            geb.setAccessible(true);
            quarkEventBus = geb.invoke(quarkInst);

            Class<?> ebClass = quarkEventBus.getClass();
            postMethod = ebClass.getDeclaredMethod("post", Object.class);
            postMethod.setAccessible(true);

            System.out.println("[Quark Hook] Wired to Quark EventBus - full module support active.");
        } catch (Exception e) {
            System.out.println("[Quark Hook] Quark EventBus not available - using standalone hook mode.");
        }
    }

    // ── Called by instrumented Minecraft methods ──────────────────────────────

    public static void onTick(Object mc) {
        mcInstance = mc;
        if (quarkEventBus != null) {
            try {
                postMethod.invoke(quarkEventBus, createEvent("cc.quark.event.events.EventTick", mc.getClass().getClassLoader()));
            } catch (Exception ignored) {}
        }
        for (TickListener l : TICK) { try { l.onTick(mc); } catch (Exception ignored) {} }
    }

    public static void onRender2D(Object ctx, float delta) {
        if (quarkEventBus != null) {
            try {
                Object evt = createEvent("cc.quark.event.events.EventRender2D", ctx.getClass().getClassLoader());
                if (evt != null) {
                    setField(evt, "drawContext", ctx);
                    setField(evt, "tickDelta", delta);
                    postMethod.invoke(quarkEventBus, evt);
                }
            } catch (Exception ignored) {}
        }
        for (Render2DListener l : RENDER2D) { try { l.onRender2D(ctx, delta); } catch (Exception ignored) {} }
    }

    public static void onRender3D(Object renderer) {
        if (quarkEventBus != null) {
            try {
                postMethod.invoke(quarkEventBus, createEvent("cc.quark.event.events.EventRender3D", renderer.getClass().getClassLoader()));
            } catch (Exception ignored) {}
        }
        for (Render3DListener l : RENDER3D) { try { l.onRender3D(renderer); } catch (Exception ignored) {} }
    }

    public static void onPacketSend(Object packet) {
        if (quarkEventBus != null) {
            try {
                Object evt = createEvent("cc.quark.event.events.EventPacketSend", packet.getClass().getClassLoader());
                if (evt != null) {
                    setField(evt, "packet", packet);
                    postMethod.invoke(quarkEventBus, evt);
                }
            } catch (Exception ignored) {}
        }
        for (PacketListener l : PACKET) { try { l.onPacket(packet); } catch (Exception ignored) {} }
    }

    public static void onKey(int key, int action) {
        if (action != 1 && action != 2) return; // PRESS or REPEAT only
        if (quarkEventBus != null) {
            try {
                Object evt = createEvent("cc.quark.event.events.EventKey", Thread.currentThread().getContextClassLoader());
                if (evt != null) {
                    setField(evt, "keyCode", key);
                    postMethod.invoke(quarkEventBus, evt);
                }
            } catch (Exception ignored) {}
        }
        for (KeyListener l : KEY) { try { l.onKey(key, action); } catch (Exception ignored) {} }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Object createEvent(String className, ClassLoader cl) {
        if (cl == null) cl = Thread.currentThread().getContextClassLoader();
        try {
            Class<?> cls = Class.forName(className, false, cl);
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private static void setField(Object obj, String name, Object value) {
        try {
            Field f = findField(obj.getClass(), name);
            if (f != null) { f.setAccessible(true); f.set(obj, value); }
        } catch (Exception ignored) {}
    }

    private static Field findField(Class<?> cls, String name) {
        while (cls != null && cls != Object.class) {
            try { return cls.getDeclaredField(name); } catch (NoSuchFieldException e) { cls = cls.getSuperclass(); }
        }
        return null;
    }
}
