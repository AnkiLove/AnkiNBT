package com.ankinbt.qa;

import net.neoforged.fml.common.Mod;

/** Entry point for the development-only editor automation mod. */
@Mod("ankinbt_qa")
public final class AnkiNBTQaMod {
    public AnkiNBTQaMod() {
        if (Boolean.getBoolean("ankinbt.qa.enabled")) {
            EditorAutomationRunner.start();
        }
    }
}
