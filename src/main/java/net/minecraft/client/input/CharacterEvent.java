package net.minecraft.client.input;

public class CharacterEvent {
    private final char codePoint;
    private final int modifiers;

    public CharacterEvent(char codePoint, int modifiers) {
        this.codePoint = codePoint;
        this.modifiers = modifiers;
    }

    public char getCodePoint() {
        return this.codePoint;
    }

    public char codepoint() {
        return this.codePoint;
    }

    public int getModifiers() {
        return this.modifiers;
    }

    public int modifiers() {
        return this.modifiers;
    }
}
