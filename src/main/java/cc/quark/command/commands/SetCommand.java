package cc.quark.command.commands;

import cc.quark.command.Command;
import cc.quark.module.Module;
import cc.quark.module.ModuleManager;
import cc.quark.setting.*;

public class SetCommand extends Command {

    private final ModuleManager moduleManager;

    public SetCommand(ModuleManager moduleManager) {
        super("set", "Change a module setting value.", "set <module> <setting> <value>");
        this.moduleManager = moduleManager;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            reply("§cUsage: .set <module> <setting> <value>");
            return;
        }

        String moduleName  = args[0];
        String settingName = args[1];
        String value       = args[2];

        Module module = moduleManager.getModule(moduleName);
        if (module == null) {
            reply("§cModule not found: " + moduleName);
            return;
        }

        Setting<?> setting = null;
        for (Setting<?> s : module.getSettings()) {
            if (s.getName().equalsIgnoreCase(settingName)) {
                setting = s;
                break;
            }
        }

        if (setting == null) {
            reply("§cSetting not found: " + settingName + " in " + module.getName());
            return;
        }

        try {
            if (setting instanceof BoolSetting bs) {
                switch (value.toLowerCase()) {
                    case "true", "on"  -> bs.setValue(true);
                    case "false", "off" -> bs.setValue(false);
                    case "toggle"       -> bs.toggle();
                    default -> { reply("§cInvalid value for bool setting. Use: true/false/on/off/toggle"); return; }
                }
            } else if (setting instanceof DoubleSetting ds) {
                ds.setValue(Double.parseDouble(value));
            } else if (setting instanceof IntSetting is) {
                is.setValue(Integer.parseInt(value));
            } else if (setting instanceof ModeSetting ms) {
                ms.setValue(value);
            } else if (setting instanceof EnumSetting<?> es) {
                setEnumValue(es, value);
            } else {
                reply("§cSetting type not supported: " + setting.getClass().getSimpleName());
                return;
            }
            reply("§a" + module.getName() + " §f> §b" + setting.getName() + " §f= §e" + setting.getValue());
        } catch (NumberFormatException e) {
            reply("§cInvalid number: " + value);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setEnumValue(EnumSetting es, String value) {
        for (Enum<?> constant : es.getValues()) {
            if (constant.name().equalsIgnoreCase(value)) {
                es.setValue(constant);
                return;
            }
        }
        reply("§cInvalid enum value: " + value);
    }
}
