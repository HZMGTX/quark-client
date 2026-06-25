package cc.quark.module;

/**
 * Enum representing the category a module belongs to.
 */
public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    WORLD("World"),
    EXPLOIT("Exploit"),
    MISC("Misc"),
    STAFF("Staff");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
