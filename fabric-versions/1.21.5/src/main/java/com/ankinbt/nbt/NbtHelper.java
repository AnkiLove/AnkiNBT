/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DynamicOps
 *  net.minecraft.class_1799
 *  net.minecraft.class_2479
 *  net.minecraft.class_2481
 *  net.minecraft.class_2487
 *  net.minecraft.class_2489
 *  net.minecraft.class_2494
 *  net.minecraft.class_2495
 *  net.minecraft.class_2497
 *  net.minecraft.class_2499
 *  net.minecraft.class_2501
 *  net.minecraft.class_2503
 *  net.minecraft.class_2509
 *  net.minecraft.class_2516
 *  net.minecraft.class_2519
 *  net.minecraft.class_2520
 *  net.minecraft.class_310
 *  net.minecraft.class_5455
 *  net.minecraft.class_6903
 */
package com.ankinbt.nbt;

import com.ankinbt.compat.VersionCompat;
import com.mojang.serialization.DynamicOps;
import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.class_1799;
import net.minecraft.class_2479;
import net.minecraft.class_2481;
import net.minecraft.class_2487;
import net.minecraft.class_2489;
import net.minecraft.class_2494;
import net.minecraft.class_2495;
import net.minecraft.class_2497;
import net.minecraft.class_2499;
import net.minecraft.class_2501;
import net.minecraft.class_2503;
import net.minecraft.class_2509;
import net.minecraft.class_2516;
import net.minecraft.class_2519;
import net.minecraft.class_2520;
import net.minecraft.class_310;
import net.minecraft.class_5455;
import net.minecraft.class_6903;

public final class NbtHelper {
    private NbtHelper() {
    }

    public static Optional<class_2487> serializeItemStack(class_1799 stack) {
        class_5455 access = NbtHelper.getRegistryAccess();
        if (access == null) {
            return Optional.empty();
        }
        class_6903 ops = access.method_57093((DynamicOps)class_2509.field_11560);
        return class_1799.field_24671.encodeStart((DynamicOps)ops, (Object)stack).map(t -> (class_2487)t).resultOrPartial();
    }

    public static Optional<class_1799> deserializeItemStack(class_2487 tag) {
        class_5455 access = NbtHelper.getRegistryAccess();
        if (access == null) {
            return Optional.empty();
        }
        class_6903 ops = access.method_57093((DynamicOps)class_2509.field_11560);
        return class_1799.field_24671.parse((DynamicOps)ops, (Object)tag).resultOrPartial();
    }

    private static class_5455 getRegistryAccess() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 != null) {
            return mc.field_1687.method_30349();
        }
        return null;
    }

    public static String getTagTypeName(class_2520 tag) {
        class_2520 class_25202 = tag;
        Objects.requireNonNull(class_25202);
        class_2520 class_25203 = class_25202;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{class_2481.class, class_2516.class, class_2497.class, class_2503.class, class_2494.class, class_2489.class, class_2519.class, class_2479.class, class_2495.class, class_2501.class, class_2499.class, class_2487.class}, (Object)class_25203, n)) {
            case 0 -> {
                class_2481 ignored = (class_2481)class_25203;
                yield "Byte";
            }
            case 1 -> {
                class_2516 ignored = (class_2516)class_25203;
                yield "Short";
            }
            case 2 -> {
                class_2497 ignored = (class_2497)class_25203;
                yield "Int";
            }
            case 3 -> {
                class_2503 ignored = (class_2503)class_25203;
                yield "Long";
            }
            case 4 -> {
                class_2494 ignored = (class_2494)class_25203;
                yield "Float";
            }
            case 5 -> {
                class_2489 ignored = (class_2489)class_25203;
                yield "Double";
            }
            case 6 -> {
                class_2519 ignored = (class_2519)class_25203;
                yield "String";
            }
            case 7 -> {
                class_2479 ignored = (class_2479)class_25203;
                yield "Byte[]";
            }
            case 8 -> {
                class_2495 ignored = (class_2495)class_25203;
                yield "Int[]";
            }
            case 9 -> {
                class_2501 ignored = (class_2501)class_25203;
                yield "Long[]";
            }
            case 10 -> {
                class_2499 ignored = (class_2499)class_25203;
                yield "List";
            }
            case 11 -> {
                class_2487 ignored = (class_2487)class_25203;
                yield "Compound";
            }
            default -> "?";
        };
    }

    public static int getTagColor(class_2520 tag) {
        class_2520 class_25202 = tag;
        Objects.requireNonNull(class_25202);
        class_2520 class_25203 = class_25202;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{class_2481.class, class_2516.class, class_2497.class, class_2503.class, class_2494.class, class_2489.class, class_2519.class, class_2487.class, class_2499.class, class_2479.class, class_2495.class, class_2501.class}, (Object)class_25203, n)) {
            case 0 -> {
                class_2481 ignored = (class_2481)class_25203;
                yield -7643914;
            }
            case 1 -> {
                class_2516 ignored = (class_2516)class_25203;
                yield -10262799;
            }
            case 2 -> {
                class_2497 ignored = (class_2497)class_25203;
                yield -12877066;
            }
            case 3 -> {
                class_2503 ignored = (class_2503)class_25203;
                yield -15817239;
            }
            case 4 -> {
                class_2494 ignored = (class_2494)class_25203;
                yield -15419226;
            }
            case 5 -> {
                class_2489 ignored = (class_2489)class_25203;
                yield -15681151;
            }
            case 6 -> {
                class_2519 ignored = (class_2519)class_25203;
                yield -680437;
            }
            case 7 -> {
                class_2487 ignored = (class_2487)class_25203;
                yield -1906448;
            }
            case 8 -> {
                class_2499 ignored = (class_2499)class_25203;
                yield -7035976;
            }
            case 9 -> {
                class_2479 ignored = (class_2479)class_25203;
                yield -5796870;
            }
            case 10 -> {
                class_2495 ignored = (class_2495)class_25203;
                yield -10443270;
            }
            case 11 -> {
                class_2501 ignored = (class_2501)class_25203;
                yield -13058568;
            }
            default -> -3355444;
        };
    }

    public static class_2520 createDefault(byte typeId) {
        return switch (typeId) {
            case 1 -> class_2481.method_23233((byte)0);
            case 2 -> class_2516.method_23254((short)0);
            case 3 -> class_2497.method_23247((int)0);
            case 4 -> class_2503.method_23251((long)0L);
            case 5 -> class_2494.method_23244((float)0.0f);
            case 6 -> class_2489.method_23241((double)0.0);
            case 8 -> class_2519.method_23256((String)"");
            case 10 -> new class_2487();
            case 9 -> new class_2499();
            case 7 -> new class_2479(new byte[0]);
            case 11 -> new class_2495(new int[0]);
            case 12 -> new class_2501(new long[0]);
            default -> class_2519.method_23256((String)"");
        };
    }

    public static class_2520 parseValue(String input, class_2520 currentTag) {
        try {
            class_2520 class_25202 = currentTag;
            Objects.requireNonNull(class_25202);
            class_2520 class_25203 = class_25202;
            int n = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{class_2481.class, class_2516.class, class_2497.class, class_2503.class, class_2494.class, class_2489.class, class_2519.class}, (Object)class_25203, n)) {
                case 0 -> {
                    class_2481 ignored = (class_2481)class_25203;
                    yield class_2481.method_23233((byte)Byte.parseByte(input));
                }
                case 1 -> {
                    class_2516 ignored = (class_2516)class_25203;
                    yield class_2516.method_23254((short)Short.parseShort(input));
                }
                case 2 -> {
                    class_2497 ignored = (class_2497)class_25203;
                    yield class_2497.method_23247((int)Integer.parseInt(input));
                }
                case 3 -> {
                    class_2503 ignored = (class_2503)class_25203;
                    yield class_2503.method_23251((long)Long.parseLong(input.replace("L", "").replace("l", "")));
                }
                case 4 -> {
                    class_2494 ignored = (class_2494)class_25203;
                    yield class_2494.method_23244((float)Float.parseFloat(input.replace("f", "").replace("F", "")));
                }
                case 5 -> {
                    class_2489 ignored = (class_2489)class_25203;
                    yield class_2489.method_23241((double)Double.parseDouble(input.replace("d", "").replace("D", "")));
                }
                case 6 -> {
                    class_2519 ignored = (class_2519)class_25203;
                    yield class_2519.method_23256((String)input);
                }
                default -> null;
            };
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getValueAsString(class_2520 tag) {
        VersionCompat vc = VersionCompat.get();
        class_2520 class_25202 = tag;
        Objects.requireNonNull(class_25202);
        class_2520 class_25203 = class_25202;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{class_2481.class, class_2516.class, class_2497.class, class_2503.class, class_2494.class, class_2489.class, class_2519.class, class_2479.class, class_2495.class, class_2501.class, class_2487.class, class_2499.class}, (Object)class_25203, n)) {
            case 0 -> {
                class_2481 b = (class_2481)class_25203;
                yield Byte.toString(vc.getByteValue(b));
            }
            case 1 -> {
                class_2516 s = (class_2516)class_25203;
                yield Short.toString(vc.getShortValue(s));
            }
            case 2 -> {
                class_2497 i = (class_2497)class_25203;
                yield Integer.toString(vc.getIntValue(i));
            }
            case 3 -> {
                class_2503 l = (class_2503)class_25203;
                yield Long.toString(vc.getLongValue(l));
            }
            case 4 -> {
                class_2494 f = (class_2494)class_25203;
                yield Float.toString(vc.getFloatValue(f));
            }
            case 5 -> {
                class_2489 d = (class_2489)class_25203;
                yield Double.toString(vc.getDoubleValue(d));
            }
            case 6 -> {
                class_2519 s = (class_2519)class_25203;
                yield vc.getStringValue(s);
            }
            case 7 -> {
                class_2479 ba = (class_2479)class_25203;
                yield "[" + ba.size() + " bytes]";
            }
            case 8 -> {
                class_2495 ia = (class_2495)class_25203;
                yield "[" + ia.size() + " ints]";
            }
            case 9 -> {
                class_2501 la = (class_2501)class_25203;
                yield "[" + la.size() + " longs]";
            }
            case 10 -> {
                class_2487 c = (class_2487)class_25203;
                yield "{" + c.method_10546() + " entries}";
            }
            case 11 -> {
                class_2499 l = (class_2499)class_25203;
                yield "[" + l.size() + " entries]";
            }
            default -> "";
        };
    }
}

