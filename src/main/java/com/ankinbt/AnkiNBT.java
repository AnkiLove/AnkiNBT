/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.neoforged.bus.api.IEventBus
 *  net.neoforged.fml.ModContainer
 *  net.neoforged.fml.common.Mod
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.ankinbt;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.keybind.KeyBindings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value="ankinbt")
public class AnkiNBT {
    public static final String MOD_ID = "ankinbt";
    public static final Logger LOGGER = LoggerFactory.getLogger((String)"AnkiNBT");

    public AnkiNBT(IEventBus modEventBus, ModContainer modContainer) {
        AnkiConfig.init();
        KeyBindings.register(modEventBus);
        LOGGER.info("AnkiNBT client initialized");
    }
}

