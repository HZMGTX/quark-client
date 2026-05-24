package com.ghostclient.util;

import java.awt.Color;

public class ColorUtil {

    public static int rainbow(int offset, float alpha) {
        float hue = (float)((System.currentTimeMillis() + offset) % 2000L) / 2000f;
        Color c = Color.getHSBColor(hue, 0.9f, 1.0f);
        return toRGBA(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha));
    }

    public static int rainbowModule(int index) {
        float hue = (float)((System.currentTimeMillis() + (long)(index * 150)) % 2000L) / 2000f;
        Color c = Color.getHSBColor(hue, 0.8f, 1.0f);
        return toRGBA(c.getRed(), c.getGreen(), c.getBlue(), 255);
    }

    public static int toRGBA(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static int[] fromARGB(int color) {
        return new int[]{
            (color >> 24) & 0xFF,
            (color >> 16) & 0xFF,
            (color >> 8) & 0xFF,
            color & 0xFF
        };
    }

    public static int lerp(int colorA, int colorB, float t) {
        int[] a = fromARGB(colorA);
        int[] b = fromARGB(colorB);
        return toRGBA(
            (int)(a[1] + (b[1] - a[1]) * t),
            (int)(a[2] + (b[2] - a[2]) * t),
            (int)(a[3] + (b[3] - a[3]) * t),
            (int)(a[0] + (b[0] - a[0]) * t)
        );
    }

    public static int fade(int color, float alpha) {
        int[] c = fromARGB(color);
        return toRGBA(c[1], c[2], c[3], (int)(alpha * 255));
    }

    public static int gradient(int[] colors, float t) {
        if (colors.length == 0) return 0xFFFFFFFF;
        if (colors.length == 1) return colors[0];
        float scaled = t * (colors.length - 1);
        int idx = (int) scaled;
        if (idx >= colors.length - 1) return colors[colors.length - 1];
        return lerp(colors[idx], colors[idx + 1], scaled - idx);
    }

    public static String toHex(int color) {
        return String.format("#%08X", color);
    }

    public static int fromHex(String hex) {
        hex = hex.replace("#", "").replace("0x", "").replace("0X", "");
        return (int) Long.parseLong(hex, 16);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }
}
