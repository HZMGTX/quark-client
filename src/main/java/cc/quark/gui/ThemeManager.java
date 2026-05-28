package cc.quark.gui;

public class ThemeManager {
    public static final ThemeManager INSTANCE = new ThemeManager();

    public enum Theme { DEFAULT, CATPPUCCIN, NORD, ONE_DARK, DRACULA, ROSE_PINE }

    private Theme currentTheme = Theme.DEFAULT;

    public int getAccentColor() {
        return switch(currentTheme) {
            case CATPPUCCIN -> 0xFFCBA6F7;
            case NORD       -> 0xFF88C0D0;
            case ONE_DARK   -> 0xFF61AFEF;
            case DRACULA    -> 0xFFBD93F9;
            case ROSE_PINE  -> 0xFFEB6F92;
            default         -> 0xFF9B59B6;
        };
    }

    public int getBackgroundColor() {
        return switch(currentTheme) {
            case CATPPUCCIN -> 0xE0181825;
            case NORD       -> 0xE02E3440;
            case ONE_DARK   -> 0xE0282C34;
            case DRACULA    -> 0xE0282A36;
            case ROSE_PINE  -> 0xE0191724;
            default         -> 0xE0141414;
        };
    }

    public int getPanelColor() {
        return switch(currentTheme) {
            case CATPPUCCIN -> 0xE01E1E2E;
            case NORD       -> 0xE03B4252;
            case ONE_DARK   -> 0xE0353B45;
            case DRACULA    -> 0xE0343746;
            case ROSE_PINE  -> 0xE01F1D2E;
            default         -> 0xE01A1A1A;
        };
    }

    public int getTextColor()          { return 0xFFFFFFFF; }
    public int getSecondaryTextColor() { return 0xFFAAAAAA; }

    public Theme getCurrentTheme()     { return currentTheme; }
    public void setTheme(Theme t)      { this.currentTheme = t; }
    public String getThemeName()       {
        String s = currentTheme.name();
        return s.charAt(0) + s.substring(1).toLowerCase().replace("_", " ");
    }
}
