package com.draconincdomain.mapcontrol.Utils;

import net.kyori.adventure.text.format.TextColor;

public class ColourUtil {

    public enum CustomColour {
        GREEN(0x00FF00),
        RED(0xFF0000),
        BLUE(0x0000FF),
        GOLD(0xFFD700),
        DARK_RED(0x8B0000),
        DARK_BLUE(0x00008B),
        REG_GREEN(0x00AA00),
        YELLOW(0xFFFF00),
        LIGHT_PURPLE(0xFFFF00),
        DARK_PURPLE(0xAA00AA),
        AQUA(0x00FFFF),
        ORANGE(0xFFA500),
        WHITE(0xFFFFFF),
        BLACK(0x000000);

        private final int rgbValue;

        CustomColour(int rgbValue) {
            this.rgbValue = rgbValue;
        }

        public TextColor getTextColour() {
            return TextColor.color(rgbValue);
        }

        public int getRgbValue() {
            return rgbValue;
        }
    }

    public static TextColor fromRGB(int red, int green, int blue) {
        return TextColor.color(red, green, blue);
    }

    public static TextColor fromEnum(CustomColour colour) {
        return colour.getTextColour();
    }
}
