package com.ankinbt;

import com.ankinbt.config.AnkiConfig;
import com.ankinbt.keybind.KeyBindings;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(AnkiNBT.MOD_ID)
public class AnkiNBT {
    public static final String MOD_ID = "ankinbt";
    public static final Logger LOGGER = LoggerFactory.getLogger("AnkiNBT");

    public AnkiNBT(IEventBus modEventBus, ModContainer modContainer) {
        AnkiConfig.init();
        KeyBindings.register(modEventBus);
        LOGGER.info("AnkiNBT client initialized");
    }
}
