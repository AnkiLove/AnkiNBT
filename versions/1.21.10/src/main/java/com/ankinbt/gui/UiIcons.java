package com.ankinbt.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.ResourceLocation;

final class UiIcons {
    static final String BOX = "\uEAA5";
    static final String SPARKLES = "\uEE59";
    static final String TEXT = "\uEEBC";
    static final String EYE = "\uEBC2";
    static final String CODE = "\uEB4C";
    static final String SAVE = "\uEE09";
    static final String CLOSE = "\uEEF9";
    static final String CHEVRON_DOWN = "\uEAFB";
    static final String CHEVRON_UP = "\uEB0F";
    static final String TAG = "\uEE7A";
    static final String BOOK = "\uEA93";
    static final String LAYERS = "\uEC6A";
    static final String SHIELD = "\uEE31";
    static final String SEARCH = "\uEE1C";
    static final String MOVE = "\uED83";
    static final String SETTINGS = "\uEB54";
    static final String HELP = "\uEDDF";
    static final String PLUS = "\uEDD7";
    static final String COPY = "\uEB60";
    static final String TRASH = "\uEEA4";
    static final String CHEVRON_LEFT = "\uEAFF";
    static final String CHEVRON_RIGHT = "\uEB03";
    static final String RESET = "\uEDEF";
    static final String INFO = "\uEC58";
    static final String USER = "\uEECF";
    static final String HEART = "\uEC3D";
    static final String ACTIVITY = "\uEA05";
    static final String LIST = "\uED34";
    static final String CART = "\uEAC2";
    static final String REPEAT = "\uEDF0";

    private static final FontDescription.Resource FONT = new FontDescription.Resource(
            ResourceLocation.fromNamespaceAndPath("ankinbt", "icons"));

    private UiIcons() {
    }

    static Component component(String glyph) {
        return Component.literal(glyph).withStyle(style -> style.withFont(FONT));
    }
}
