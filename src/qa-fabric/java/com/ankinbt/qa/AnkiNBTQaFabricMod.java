package com.ankinbt.qa;

import net.fabricmc.api.ClientModInitializer;

/** Fabric entry point for the development-only editor automation mod. */
public final class AnkiNBTQaFabricMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EditorAutomationRunner.start();
    }
}
