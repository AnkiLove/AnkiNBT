/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.ClientModInitializer
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.ankinbt;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.keybind.KeyBindings;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnkiNBTFabric
implements ClientModInitializer {
    public static final String MOD_ID = "ankinbt";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"AnkiNBT");

    public void onInitializeClient() {
        AnkiConfig.init();
        KeyBindings.register();
        LOGGER.info("AnkiNBT (Fabric) loaded - Press N to open NBT editor");
    }
}

