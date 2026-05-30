package cc.quark.setting;

public class StringSetting extends Setting<String> {

    public StringSetting(String name, String description, String defaultValue) {
        super(name, description, defaultValue);
    }

    @Override
    public void setValue(String value) {
        this.value = value != null ? value : "";
    }

    public String get() {
        return value;
    }
}
