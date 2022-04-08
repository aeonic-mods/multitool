package design.aeonic.multitool.api;

import design.aeonic.multitool.EngineersMultitool;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.commons.lang3.StringUtils;

public final class Constants {
    public static final String MOD_ID = "multitool";

    public static class Styles {
        public static final Style TOOLTIP_PLAIN = Style.EMPTY.withColor(ChatFormatting.GRAY);
        public static final Style TOOLTIP_SETTING = Style.EMPTY.withColor(ChatFormatting.BLUE);
        public static final Style TOOLTIP_KEYBIND = Style.EMPTY.withColor(ChatFormatting.YELLOW).withBold(true);
    }

    public static class Translations {
        // Multitool
        public static final TranslatableComponent MULTITOOL_MODE = make("Mode: ","gui", MOD_ID, "multitool.mode");
        public static final TranslatableComponent MULTITOOL_EMPTY = make("Empty", "gui", MOD_ID, "multitool.behavior.empty");
        public static final TranslatableComponent MULTITOOL_DEBUG = make("Debug", "gui", MOD_ID, "multitool.behavior.debug");
        public static final TranslatableComponent MULTITOOL_MACHINE_BUILDING = make("Machine Building", "gui", MOD_ID, "multitool.behavior.machine_building");

        // Screens
        public static final TranslatableComponent MULTITOOL_SELECTION_SCREEN = make("Multitool Mode Select", "gui", MOD_ID, "screen.multitool.select");

        public static TranslatableComponent make(String english, String... path) {
            return make(english, StringUtils.join(path, "."));
        }

        public static TranslatableComponent make(String english, String key) {
            return EngineersMultitool.REGISTRATE.addRawLang(key, english);
        }
        public static TranslatableComponentSupplier makeDynamic(String english, String... path) {
            return makeDynamic(english, StringUtils.join(path, "."));
        }

        public static TranslatableComponentSupplier makeDynamic(String english, String key) {
            EngineersMultitool.REGISTRATE.addRawLang(key, english);
            return new TranslatableComponentSupplier(key);
        }

        public record TranslatableComponentSupplier(String key) {
            public TranslatableComponent resolve(Object... args) {
                return new TranslatableComponent(key, args);
            }
        }

        public static void load() {}
    }
}
