package com.ankinbt.nbt;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import com.ankinbt.compat.VersionCompat;
import java.util.Optional;

/**
 * NBT utility methods. Uses ItemStack.CODEC with RegistryOps for proper
 * 1.21+ data component serialization (following NBTEdit's approach).
 */
public final class NbtHelper {

    private NbtHelper() {}

    // ==================== ItemStack Codec ====================

    /**
     * Serializes an ItemStack to a full CompoundTag using ItemStack.CODEC.
     * This captures ALL data components (enchantments, damage, custom_data, etc.)
     * as a single editable NBT tree — the correct approach for 1.21+.
     */
    public static Optional<CompoundTag> serializeItemStack(ItemStack stack) {
        RegistryAccess access = getRegistryAccess();
        if (access == null) return Optional.empty();
        RegistryOps<Tag> ops = access.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.CODEC.encodeStart(ops, stack)
                .map(t -> (CompoundTag) t)
                .resultOrPartial();
    }

    /**
     * Deserializes a CompoundTag back to an ItemStack using ItemStack.CODEC.
     */
    public static Optional<ItemStack> deserializeItemStack(CompoundTag tag) {
        RegistryAccess access = getRegistryAccess();
        if (access == null) return Optional.empty();
        RegistryOps<Tag> ops = access.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.CODEC.parse(ops, tag).resultOrPartial();
    }

    private static RegistryAccess getRegistryAccess() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) return mc.level.registryAccess();
        return null;
    }

    // ==================== Tag Type Info ====================

    public static String getTagTypeName(Tag tag) {
        return switch (tag) {
            case ByteTag ignored -> "Byte";
            case ShortTag ignored -> "Short";
            case IntTag ignored -> "Int";
            case LongTag ignored -> "Long";
            case FloatTag ignored -> "Float";
            case DoubleTag ignored -> "Double";
            case StringTag ignored -> "String";
            case ByteArrayTag ignored -> "Byte[]";
            case IntArrayTag ignored -> "Int[]";
            case LongArrayTag ignored -> "Long[]";
            case ListTag ignored -> "List";
            case CompoundTag ignored -> "Compound";
            default -> "?";
        };
    }

    public static int getTagColor(Tag tag) {
        return switch (tag) {
            case ByteTag ignored -> 0xFF8B5CF6;    // purple
            case ShortTag ignored -> 0xFF6366F1;   // indigo
            case IntTag ignored -> 0xFF3B82F6;     // blue
            case LongTag ignored -> 0xFF0EA5E9;    // sky
            case FloatTag ignored -> 0xFF14B8A6;   // teal
            case DoubleTag ignored -> 0xFF10B981;  // emerald
            case StringTag ignored -> 0xFFF59E0B;  // amber
            case CompoundTag ignored -> 0xFFE2E8F0;// slate
            case ListTag ignored -> 0xFF94A3B8;    // slate-400
            case ByteArrayTag ignored -> 0xFFA78BFA;
            case IntArrayTag ignored -> 0xFF60A5FA;
            case LongArrayTag ignored -> 0xFF38BDF8;
            default -> 0xFFCCCCCC;
        };
    }

    // ==================== Tag Creation ====================

    public static Tag createDefault(byte typeId) {
        return switch (typeId) {
            case Tag.TAG_BYTE -> ByteTag.valueOf((byte) 0);
            case Tag.TAG_SHORT -> ShortTag.valueOf((short) 0);
            case Tag.TAG_INT -> IntTag.valueOf(0);
            case Tag.TAG_LONG -> LongTag.valueOf(0L);
            case Tag.TAG_FLOAT -> FloatTag.valueOf(0f);
            case Tag.TAG_DOUBLE -> DoubleTag.valueOf(0d);
            case Tag.TAG_STRING -> StringTag.valueOf("");
            case Tag.TAG_COMPOUND -> new CompoundTag();
            case Tag.TAG_LIST -> new ListTag();
            case Tag.TAG_BYTE_ARRAY -> new ByteArrayTag(new byte[0]);
            case Tag.TAG_INT_ARRAY -> new IntArrayTag(new int[0]);
            case Tag.TAG_LONG_ARRAY -> new LongArrayTag(new long[0]);
            default -> StringTag.valueOf("");
        };
    }

    // ==================== Tag Parsing ====================

    /**
     * Parses a string value into the appropriate tag type.
     * Follows NBTEdit's TagParseHelper approach for value display/edit.
     */
    public static Tag parseValue(String input, Tag currentTag) {
        try {
            return switch (currentTag) {
                case ByteTag ignored -> ByteTag.valueOf(Byte.parseByte(input));
                case ShortTag ignored -> ShortTag.valueOf(Short.parseShort(input));
                case IntTag ignored -> IntTag.valueOf(Integer.parseInt(input));
                case LongTag ignored -> LongTag.valueOf(Long.parseLong(input.replace("L", "").replace("l", "")));
                case FloatTag ignored -> FloatTag.valueOf(Float.parseFloat(input.replace("f", "").replace("F", "")));
                case DoubleTag ignored -> DoubleTag.valueOf(Double.parseDouble(input.replace("d", "").replace("D", "")));
                case StringTag ignored -> StringTag.valueOf(input);
                default -> null;
            };
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets the display value of a tag (following NBTEdit's TagParseHelper.getValueAsString).
     */
    public static String getValueAsString(Tag tag) {
        VersionCompat vc = VersionCompat.get();
        return switch (tag) {
            case ByteTag b -> Byte.toString(vc.getByteValue(b));
            case ShortTag s -> Short.toString(vc.getShortValue(s));
            case IntTag i -> Integer.toString(vc.getIntValue(i));
            case LongTag l -> Long.toString(vc.getLongValue(l));
            case FloatTag f -> Float.toString(vc.getFloatValue(f));
            case DoubleTag d -> Double.toString(vc.getDoubleValue(d));
            case StringTag s -> vc.getStringValue(s);
            case ByteArrayTag ba -> "[" + ba.size() + " bytes]";
            case IntArrayTag ia -> "[" + ia.size() + " ints]";
            case LongArrayTag la -> "[" + la.size() + " longs]";
            case CompoundTag c -> "{" + c.size() + " entries}";
            case ListTag l -> "[" + l.size() + " entries]";
            default -> "";
        };
    }
}
