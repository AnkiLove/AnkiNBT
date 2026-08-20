package com.ankinbt.qa;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.editor.SpawnEggEditorHelper;
import com.ankinbt.gui.EntityEditorScreen;
import com.ankinbt.gui.SimpleEditorScreen;
import com.ankinbt.gui.VillagerTradeEditorScreen;
import com.ankinbt.nbt.NbtFileIO;
import com.ankinbt.nbt.NbtHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Drives the real editor classes inside a running integrated client. The test
 * mod is isolated from release artifacts by the Gradle qa source set.
 */
public final class EditorAutomationRunner {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final AtomicBoolean STARTED = new AtomicBoolean();
    private static final List<Result> RESULTS = new ArrayList<>();
    private static final List<Entity> CREATED_ENTITIES = new ArrayList<>();
    private static final long TIMEOUT_SECONDS = Long.getLong("ankinbt.qa.timeoutSeconds", 240L);
    private static final String LOADER = System.getProperty("ankinbt.qa.loader", "unknown");
    private static final String VERSION = System.getProperty("ankinbt.qa.version", "unknown");
    private static final String LANGUAGE = System.getProperty("ankinbt.qa.language", "zh_cn");
    private static final Path REPORT = Path.of(System.getProperty("ankinbt.qa.report", "ankinbt-qa-report.json"))
            .toAbsolutePath().normalize();
    private static final Path SCREENSHOT_DIR = Path.of(System.getProperty(
                    "ankinbt.qa.screenshotDir", "ankinbt-qa-screenshots"))
            .toAbsolutePath().normalize();
    private static final Instant STARTED_AT = Instant.now();
    private static final List<String> CAPTURES = new ArrayList<>();

    private static volatile boolean finished;
    private static volatile Throwable fatalError;
    private static volatile SimpleEditorScreen itemScreen;
    private static volatile EntityEditorScreen spawnEggEntityScreen;
    private static volatile EntityEditorScreen liveEntityScreen;
    private static volatile VillagerTradeEditorScreen tradeScreen;

    private EditorAutomationRunner() {
    }

    public static void start() {
        if (!STARTED.compareAndSet(false, true)) return;
        Thread worker = new Thread(EditorAutomationRunner::run, "AnkiNBT-QA-Runner");
        worker.setDaemon(true);
        worker.start();
    }

    private static void run() {
        writeReport("STARTING");
        try {
            Minecraft mc = waitForClient();
            ensureWorld(mc);
            check("基础设施", "集成客户端与单人服务器已就绪", () -> {
                require(mc.level != null, "客户端世界为空");
                require(mc.player != null, "客户端玩家为空");
                require(mc.getSingleplayerServer() != null, "集成服务器为空");
            });
            runDisplayEnvironmentChecks();

            runItemChecks();
            runEntityChecks();
            runTradeChecks();
            runScreenLifecycleChecks();
        } catch (Throwable error) {
            fatalError = rootCause(error);
            record("基础设施", "自动化流程未正常完成", false, describe(fatalError));
        } finally {
            cleanupEntities();
            finished = true;
            boolean passed = fatalError == null && RESULTS.stream().allMatch(Result::passed);
            writeReport(passed ? "PASSED" : "FAILED");
            stopClient();
        }
    }

    private static void runItemChecks() throws Exception {
        itemScreen = new SimpleEditorScreen(new ItemStack(Items.DIAMOND_SWORD));
        showAndCycle(itemScreen, "activeCat", "item", 180L);

        check("物品", "重命名与颜色代码", () -> onClient(() -> {
            call(itemScreen, "applyInlineEdit", "rename", "&bAnki QA Blade", false);
            ItemStack stack = editedStack(itemScreen);
            require(stack.getHoverName().getString().contains("Anki QA Blade"), "名称没有写入");
            return null;
        }));
        check("物品", "数量、耐久与最大耐久", () -> onClient(() -> {
            call(itemScreen, "applyInlineEdit", "count", "7", false);
            call(itemScreen, "applyInlineEdit", "damage", "12", false);
            call(itemScreen, "applyInlineEdit", "max_damage", "4096", false);
            ItemStack stack = editedStack(itemScreen);
            require(stack.getCount() == 7, "数量不是7");
            require(stack.getDamageValue() == 12, "损坏值不是12");
            require(stack.getMaxDamage() == 4096, "最大耐久不是4096");
            return null;
        }));
        check("物品", "修复惩罚与自定义模型数据", () -> onClient(() -> {
            call(itemScreen, "applyInlineEdit", "repair_cost", "23", false);
            // 1.21.4+ stores custom model data as IEEE-754 floats. Keep the
            // integer inside the exact float range so the same semantic value
            // can be asserted across both the old int and new list formats.
            call(itemScreen, "applyInlineEdit", "custom_model_data", "12345678", false);
            require(((Number) call(itemScreen, "getRepairCost")).intValue() == 23, "修复惩罚不匹配");
            require(((Number) call(itemScreen, "getCustomModelData")).intValue() == 12_345_678, "模型数据不匹配");
            return null;
        }));
        check("物品", "不可破坏、耐火与附魔光效开关", () -> onClient(() -> {
            call(itemScreen, "toggleUnbreakable");
            call(itemScreen, "toggleFireResistant");
            call(itemScreen, "toggleEnchantGlint");
            require((Boolean) call(itemScreen, "isUnbreakable"), "不可破坏未开启");
            require((Boolean) call(itemScreen, "isFireResistant"), "耐火未开启");
            require((Boolean) call(itemScreen, "hasEnchantGlint"), "附魔光效未开启");
            return null;
        }));
        check("物品", "隐藏提示与附加提示开关", () -> onClient(() -> {
            if (VersionCompat.get().hasHideTooltipFeature()) {
                call(itemScreen, "toggleHideTooltip");
                require((Boolean) call(itemScreen, "isHideTooltip"), "隐藏提示未开启");
            }
            if (VersionCompat.get().hasHideAdditionalFeature()) {
                call(itemScreen, "toggleHideAdditional");
                require((Boolean) call(itemScreen, "isHideAdditional"), "隐藏附加提示未开启");
            }
            return null;
        }));
        check("物品", "稀有度循环", () -> onClient(() -> {
            call(itemScreen, "cycleRarity");
            call(itemScreen, "cycleRarity");
            require(!((List<?>) call(itemScreen, "getGeneralRows")).isEmpty(), "通用编辑行为空");
            return null;
        }));
        check("物品", "Lore新增、编辑、排序、删除与清空", () -> onClient(() -> {
            call(itemScreen, "applyInlineEdit", "lore_add", "第一行\n第二行", true);
            List<?> lore = (List<?>) call(itemScreen, "getLore");
            require(lore.size() == 2, "Lore新增数量错误");
            call(itemScreen, "applyInlineEdit", "lore:0", "已编辑", true);
            call(itemScreen, "moveLore", 0, 1);
            call(itemScreen, "removeLore", 0);
            require(((List<?>) call(itemScreen, "getLore")).size() == 1, "Lore删除失败");
            call(itemScreen, "clearLore");
            require(((List<?>) call(itemScreen, "getLore")).isEmpty(), "Lore清空失败");
            return null;
        }));
        check("物品", "附魔注册表唯一性与lunge去重", () -> onClient(() -> {
            List<String> ids = VersionCompat.get().getAllEnchantIds();
            require(!ids.isEmpty(), "附魔注册表为空");
            long unique = ids.stream().distinct().count();
            require(unique == ids.size(), "附魔注册表存在重复ID");
            long lunge = ids.stream().filter("minecraft:lunge"::equals).count();
            require(lunge <= 1L, "minecraft:lunge出现" + lunge + "次");
            return null;
        }));
        check("物品", "附魔新增、改级、删除与清空", () -> onClient(() -> {
            require((Boolean) call(itemScreen, "applyEnchantLevel", "minecraft:sharpness", 5), "锋利附魔新增失败");
            require(((java.util.Set<?>) call(itemScreen, "appliedEnchantIds")).contains("minecraft:sharpness"), "锋利附魔未读回");
            call(itemScreen, "applyInlineEdit", "ench_level:minecraft:sharpness", "3", false);
            call(itemScreen, "removeEnchantment", "minecraft:sharpness");
            require(!((java.util.Set<?>) call(itemScreen, "appliedEnchantIds")).contains("minecraft:sharpness"), "附魔删除失败");
            call(itemScreen, "clearEnchantments");
            return null;
        }));
        check("物品", "属性新增、数值编辑、删除与清空", () -> onClient(() -> {
            Method add = method(itemScreen.getClass(), "addAttribute", 4, null);
            Object operation = enumConstant(add.getParameterTypes()[2], "ADD_VALUE", "ADDITION");
            Object slot = enumConstant(add.getParameterTypes()[3], "MAINHAND", "MAIN_HAND", "ANY");
            add.invoke(itemScreen, "minecraft:attack_damage", 6.5D, operation, slot);
            List<?> rows = (List<?>) call(itemScreen, "getAttributeRows");
            require(rows.size() >= 2, "属性没有加入编辑器");
            call(itemScreen, "applyInlineEdit", "attr_amount:0", "9.25", false);
            call(itemScreen, "removeAttribute", 0);
            call(itemScreen, "clearAttributes");
            return null;
        }));
        check("物品", "最大堆叠数量", () -> onClient(() -> {
            SimpleEditorScreen screen = new SimpleEditorScreen(new ItemStack(Items.STONE));
            call(screen, "applyInlineEdit", "max_stack", "48", false);
            require(editedStack(screen).getMaxStackSize() == 48, "最大堆叠不是48");
            return null;
        }));
        check("物品", "食物营养与饱和度", () -> onClient(() -> {
            SimpleEditorScreen screen = new SimpleEditorScreen(new ItemStack(Items.BREAD));
            call(screen, "applyInlineEdit", "food_nutrition", "12", false);
            call(screen, "applyInlineEdit", "food_saturation", "1.5", false);
            ItemStack stack = editedStack(screen);
            require(VersionCompat.get().getFoodNutrition(stack) == 12, "营养值不是12");
            require(Math.abs(VersionCompat.get().getFoodSaturation(stack) - 1.5F) < 0.001F, "饱和度不是1.5");
            return null;
        }));
        check("物品", "皮革染色", () -> onClient(() -> {
            SimpleEditorScreen screen = new SimpleEditorScreen(new ItemStack(Items.LEATHER_CHESTPLATE));
            call(screen, "applyInlineEdit", "dye_color", "12ABEF", false);
            require(!((List<?>) call(screen, "getVisualRows")).isEmpty(), "染色后外观行为空");
            return null;
        }));
        check("物品", "药水类型、颜色与自定义效果", () -> onClient(() -> {
            SimpleEditorScreen screen = new SimpleEditorScreen(new ItemStack(Items.POTION));
            call(screen, "setPotionBase", "minecraft:healing");
            call(screen, "setPotionCustomColor", 0x33AAEE);
            call(screen, "addPotionCustomEffect", "minecraft:speed", 600, 2, false, true, true);
            require("minecraft:healing".equals(call(screen, "getPotionId")), "基础药水类型不匹配");
            require(((Number) call(screen, "getPotionCustomColor")).intValue() == 0x33AAEE, "药水颜色不匹配");
            require(((Number) call(screen, "getPotionCustomEffectCount")).intValue() == 1, "自定义效果数量不匹配");
            call(screen, "clearPotionCustomEffects");
            call(screen, "clearPotionCustomColor");
            return null;
        }));
        check("物品", "容器与收纳袋识别", () -> onClient(() -> {
            require((Boolean) call(itemScreen, "supportsContainerPreview", new ItemStack(Items.CHEST)), "箱子未识别为容器");
            require((Boolean) call(itemScreen, "supportsContainerPreview", new ItemStack(Items.BUNDLE)), "收纳袋未识别为容器");
            return null;
        }));
        check("物品", "完整组件序列化与反序列化", () -> onClient(() -> {
            ItemStack source = editedStack(itemScreen);
            CompoundTag tag = NbtHelper.serializeItemStack(source).orElseThrow(() -> new AssertionError("序列化返回空"));
            ItemStack decoded = NbtHelper.deserializeItemStack(tag).orElseThrow(() -> new AssertionError("反序列化返回空"));
            require(decoded.getHoverName().getString().contains("Anki QA Blade"), "往返后名称丢失");
            require(decoded.getMaxDamage() == source.getMaxDamage(), "往返后最大耐久丢失");
            return null;
        }));
        check("物品", "NBT文件导出与导入", () -> onClient(() -> {
            CompoundTag tag = NbtHelper.serializeItemStack(editedStack(itemScreen))
                    .orElseThrow(() -> new AssertionError("导出前序列化失败"));
            Path file = REPORT.getParent().resolve("item-editor-roundtrip.nbt");
            Path written = NbtFileIO.exportNbtToPath(tag, file, "AnkiNBT QA");
            require(written != null && Files.isRegularFile(written), "NBT文件没有写入");
            CompoundTag loaded = NbtFileIO.importNbt(written);
            require(loaded != null && !loaded.isEmpty(), "NBT文件没有读回");
            return null;
        }));
    }

    private static void runDisplayEnvironmentChecks() {
        check("显示", "全屏、GUI缩放4与语言环境", () -> {
            onClient(() -> {
                Minecraft mc = Minecraft.getInstance();
                Object fullscreenOption = callIfPresent(mc.options, "fullscreen");
                if (fullscreenOption == null) fullscreenOption = getField(mc.options, "fullscreen");
                callIfPresent(fullscreenOption, "set", Boolean.TRUE);
                Object window = mc.getWindow();
                if (!Boolean.TRUE.equals(callIfPresent(window, "isFullscreen"))) {
                    call(window, "toggleFullScreen");
                }
                return null;
            });
            long fullscreenDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5L);
            while (!Boolean.TRUE.equals(onClient(() ->
                    callIfPresent(Minecraft.getInstance().getWindow(), "isFullscreen")))
                    && System.nanoTime() < fullscreenDeadline) {
                sleep(100L);
            }
            onClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            Object window = mc.getWindow();
            Object fullscreen = callIfPresent(window, "isFullscreen");
            require(Boolean.TRUE.equals(fullscreen), "客户端没有以全屏模式运行");

            Object guiScaleOption = callIfPresent(mc.options, "guiScale");
            if (guiScaleOption == null) guiScaleOption = getField(mc.options, "guiScale");
            Object guiScale = callIfPresent(guiScaleOption, "get");
            require(guiScale instanceof Number && ((Number) guiScale).intValue() == 4,
                    "GUI缩放不是4，实际=" + guiScale);

            int physicalWidth = ((Number) call(window, "getWidth")).intValue();
            int physicalHeight = ((Number) call(window, "getHeight")).intValue();
            int scaledWidth = ((Number) call(window, "getGuiScaledWidth")).intValue();
            int scaledHeight = ((Number) call(window, "getGuiScaledHeight")).intValue();
            require(physicalWidth > 0 && physicalHeight > 0 && scaledWidth > 0 && scaledHeight > 0,
                    "窗口或GUI尺寸无效");
            require(physicalWidth >= scaledWidth * 3 && physicalHeight >= scaledHeight * 3,
                    "GUI缩放4没有实际生效：" + physicalWidth + "x" + physicalHeight
                            + " / " + scaledWidth + "x" + scaledHeight);

            Object languageManager = callIfPresent(mc, "getLanguageManager");
            Object selected = languageManager == null ? null : callIfPresent(languageManager, "getSelected");
            require(selected == null || LANGUAGE.equalsIgnoreCase(selected.toString()),
                    "运行语言不匹配，期望=" + LANGUAGE + "，实际=" + selected);
            return null;
            });
        });

        check("显示", "当前语言核心界面文本已解析", () -> onClient(() -> {
            for (String key : List.of(
                    "ankinbt.cat.general",
                    "ankinbt.cat.enchant",
                    "ankinbt.entity.tab.general",
                    "ankinbt.villager.tab.trade",
                    "ankinbt.villager.tab.villager")) {
                String translated = Component.translatable(key).getString();
                require(!key.equals(translated) && !translated.isBlank(), "缺少翻译：" + key);
            }
            return null;
        }));

        check("显示", "Mynaui图标字体已加载且未回退为方框", () -> onClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            Class<?> iconsClass = Class.forName("com.ankinbt.gui.UiIcons");
            Method componentMethod = iconsClass.getDeclaredMethod("component", String.class);
            componentMethod.setAccessible(true);
            Component icon = (Component) componentMethod.invoke(null, "\uEAA5");
            require(icon.getStyle().getFont() != null
                            && icon.getStyle().getFont().toString().contains("ankinbt:icons"),
                    "图标组件没有绑定 ankinbt:icons 字体");

            Object fontManager = null;
            for (Field candidate : Minecraft.class.getDeclaredFields()) {
                if (!candidate.getType().getSimpleName().equals("FontManager")) continue;
                candidate.setAccessible(true);
                fontManager = candidate.get(mc);
                break;
            }
            require(fontManager != null, "无法取得 Minecraft FontManager");

            Map<?, ?> fontSets = null;
            for (Field candidate : fontManager.getClass().getDeclaredFields()) {
                if (!Map.class.isAssignableFrom(candidate.getType())) continue;
                candidate.setAccessible(true);
                Object value = candidate.get(fontManager);
                if (!(value instanceof Map<?, ?> map)) continue;
                boolean containsIconFont = map.keySet().stream()
                        .anyMatch(key -> "ankinbt:icons".equals(String.valueOf(key)));
                if (containsIconFont) {
                    fontSets = map;
                    break;
                }
            }
            require(fontSets != null, "FontManager未注册ankinbt:icons，界面会显示缺字方框");

            int styledWidth = mc.font.width(icon);
            int fallbackWidth = mc.font.width(Component.literal("\uEAA5"));
            require(styledWidth > 0 && styledWidth != fallbackWidth,
                    "图标字形仍在使用默认PUA回退：custom=" + styledWidth + ", fallback=" + fallbackWidth);
            return null;
        }));
    }

    private static void runEntityChecks() throws Exception {
        ItemStack pigEgg = new ItemStack(Items.PIG_SPAWN_EGG);
        spawnEggEntityScreen = EntityEditorScreen.forSpawnEgg(pigEgg, 0);
        showAndCycle(spawnEggEntityScreen, "activeTab", "spawn-egg-entity", 180L);

        check("实体", "刷怪蛋类型识别", () -> onClient(() -> {
            require(SpawnEggEditorHelper.isSpawnEgg(pigEgg), "猪刷怪蛋未识别");
            require("minecraft:pig".equals(SpawnEggEditorHelper.inferEntityIdFromSpawnEgg(pigEgg)), "实体ID推断错误");
            return null;
        }));
        check("实体", "名称、生命与五项状态补丁", () -> onClient(() -> {
            setField(spawnEggEntityScreen, "stNoAi", 1);
            setField(spawnEggEntityScreen, "stInvulnerable", 1);
            setField(spawnEggEntityScreen, "stNoGravity", 1);
            setField(spawnEggEntityScreen, "stSilent", 1);
            setField(spawnEggEntityScreen, "stBaby", 1);
            setEditBox(spawnEggEntityScreen, "nameBox", "Anki QA Pig");
            setEditBox(spawnEggEntityScreen, "healthBox", "40");
            CompoundTag patch = (CompoundTag) call(spawnEggEntityScreen, "buildPatch");
            require(readBoolean(spawnEggEntityScreen, patch, "NoAI"), "NoAI补丁缺失");
            require(readBoolean(spawnEggEntityScreen, patch, "Invulnerable"), "Invulnerable补丁缺失");
            require(readBoolean(spawnEggEntityScreen, patch, "NoGravity"), "NoGravity补丁缺失");
            require(readBoolean(spawnEggEntityScreen, patch, "Silent"), "Silent补丁缺失");
            require(readNumber(patch, "Age").intValue() < 0, "幼年年龄补丁缺失");
            require(patch.contains("CustomName"), "自定义名称补丁缺失");
            require(Math.abs(readNumber(patch, "Health").floatValue() - 40F) < 0.01F, "生命值补丁错误");
            return null;
        }));
        check("实体", "刷怪蛋实体数据合并与回读", () -> onClient(() -> {
            CompoundTag patch = (CompoundTag) call(spawnEggEntityScreen, "buildPatch");
            ItemStack merged = SpawnEggEditorHelper.withMergedEntityData(pigEgg, patch)
                    .orElseThrow(() -> new AssertionError("刷怪蛋合并失败"));
            CompoundTag readBack = SpawnEggEditorHelper.getEntityData(merged)
                    .orElseThrow(() -> new AssertionError("刷怪蛋回读失败"));
            require(readBack.contains("NoAI"), "回读后NoAI丢失");
            require(readBack.contains("Health"), "回读后Health丢失");
            require("minecraft:pig".equals(readString(readBack, "id")), "回读后实体ID错误");
            return null;
        }));
        check("实体", "撤销与恢复默认状态", () -> onClient(() -> {
            call(spawnEggEntityScreen, "pushUndo");
            setField(spawnEggEntityScreen, "stSilent", 0);
            call(spawnEggEntityScreen, "undo");
            call(spawnEggEntityScreen, "resetStates");
            require(((Number) getField(spawnEggEntityScreen, "stNoAi")).intValue() == -1, "状态没有恢复为保持");
            return null;
        }));

        Entity pig = createEntity("PIG", 2.0, 90.0, 2.0);
        liveEntityScreen = EntityEditorScreen.forEntity(pig);
        showAndCycle(liveEntityScreen, "activeTab", "live-entity", 160L);
        check("实体", "真实实体异步保存与服务端回读", () -> {
            onClient(() -> {
                setField(liveEntityScreen, "stNoAi", 1);
                setField(liveEntityScreen, "stInvulnerable", 1);
                setField(liveEntityScreen, "stNoGravity", 1);
                setField(liveEntityScreen, "stSilent", 1);
                setField(liveEntityScreen, "stBaby", 1);
                setEditBox(liveEntityScreen, "nameBox", "Anki QA Live Pig");
                setEditBox(liveEntityScreen, "healthBox", "30");
                call(liveEntityScreen, "applyPatch");
                return null;
            });
            waitUntil(12_000L, () -> onServer(() -> pig.isInvulnerable()
                    && pig.isNoGravity()
                    && pig.isSilent()
                    && Boolean.TRUE.equals(call(pig, "isNoAi"))
                    && pig.getCustomName() != null
                    && pig.getCustomName().getString().contains("Anki QA Live Pig")));
        });
    }

    @SuppressWarnings("unchecked")
    private static void runTradeChecks() throws Exception {
        Entity villager = createEntity("VILLAGER", 5.0, 90.0, 5.0);
        tradeScreen = VillagerTradeEditorScreen.forEntity(villager);
        showAndCycle(tradeScreen, "activeTab", "villager-trade", 180L);

        check("交易", "新增、复制、移动、删除与撤销", () -> onClient(() -> {
            List<Object> trades = (List<Object>) getField(tradeScreen, "trades");
            call(tradeScreen, "ensureTrades");
            int initial = trades.size();
            call(tradeScreen, "addTrade");
            require(trades.size() == initial + 1, "新增交易失败");
            call(tradeScreen, "removeTrade");
            require(trades.size() == initial, "删除交易失败");
            call(tradeScreen, "duplicateTrade");
            require(trades.size() == initial + 1, "复制交易失败");
            call(tradeScreen, "moveCurrentTrade", -1);
            call(tradeScreen, "undo");
            require(!trades.isEmpty(), "撤销后交易列表为空");
            while (trades.size() > 1) {
                setField(tradeScreen, "tradeIndex", trades.size() - 1);
                call(tradeScreen, "removeTrade");
            }
            return null;
        }));
        check("交易", "买入、第二买入、卖出及全部数值字段", () -> onClient(() -> {
            configureTradeDraft(tradeScreen);
            Object offers = call(tradeScreen, "buildMerchantOffers");
            require(offers instanceof List<?> && ((List<?>) offers).size() == 1, "报价构造数量不是1");
            Object offer = ((List<?>) offers).get(0);
            require(((Number) call(offer, "getMaxUses")).intValue() == 24, "最大使用次数错误");
            require(((Number) call(offer, "getXp")).intValue() == 15, "经验值错误");
            require(((Number) call(offer, "getUses")).intValue() == 3, "已使用次数错误");
            require(((Number) call(offer, "getSpecialPriceDiff")).intValue() == -2, "特殊价格错误");
            require(((Number) call(offer, "getDemand")).intValue() == 4, "需求值错误");
            require(Math.abs(((Number) call(offer, "getPriceMultiplier")).floatValue() - 0.2F) < 0.001F, "价格倍率错误");
            require(!(Boolean) call(offer, "shouldRewardExp"), "经验奖励开关错误");
            return null;
        }));
        check("交易", "MerchantOffer序列化与配方回读", () -> onClient(() -> {
            Object offers = call(tradeScreen, "buildMerchantOffers");
            Object offer = ((List<?>) offers).get(0);
            CompoundTag recipe = (CompoundTag) call(tradeScreen, "merchantOfferToTag", offer);
            require(recipe != null && recipe.contains("buy") && recipe.contains("sell"), "报价序列化字段缺失");
            Object roundTrip = call(tradeScreen, "tradeFromRecipe", recipe);
            require("minecraft:emerald".equals(getField(roundTrip, "buyId")), "买入物品回读错误");
            require("minecraft:diamond_sword".equals(getField(roundTrip, "sellId")), "卖出物品回读错误");
            return null;
        }));
        check("交易", "真实村民报价、职业、等级与类型保存", () -> {
            String[] applyStatus = {""};
            onClient(() -> {
                configureTradeDraft(tradeScreen);
                setField(tradeScreen, "professionIndex", professionIndex(tradeScreen, "minecraft:librarian"));
                setField(tradeScreen, "villagerLevel", 4);
                setField(tradeScreen, "villagerType", "minecraft:desert");
                call(tradeScreen, "applyTrade");
                Object status = getField(tradeScreen, "status");
                applyStatus[0] = status instanceof Component component ? component.getString() : String.valueOf(status);
                return null;
            });
            try {
                waitUntil(15_000L, () -> onServer(() -> {
                    Object offers = call(villager, "getOffers");
                    return offers instanceof List<?> && ((List<?>) offers).size() == 1;
                }));
            } catch (AssertionError timeout) {
                int liveOffers = onServer(() -> {
                    Object offers = call(villager, "getOffers");
                    return offers instanceof List<?> list ? list.size() : -1;
                });
                throw new AssertionError("村民交易保存超时，界面状态=" + applyStatus[0] + "，服务端报价数=" + liveOffers);
            }
            onServer(() -> {
                Object offers = call(villager, "getOffers");
                Object offer = ((List<?>) offers).get(0);
                require(((Number) call(offer, "getMaxUses")).intValue() == 24, "服务端最大使用次数错误");
                require(((Number) call(offer, "getUses")).intValue() == 3, "服务端已使用次数错误");
                require(((Number) call(offer, "getXp")).intValue() == 15, "服务端经验值错误");
                require(((Number) call(offer, "getSpecialPriceDiff")).intValue() == -2, "服务端特殊价格错误");
                require(((Number) call(offer, "getDemand")).intValue() == 4, "服务端需求值错误");
                require(Math.abs(((Number) call(offer, "getPriceMultiplier")).floatValue() - 0.2F) < 0.001F,
                        "服务端价格倍率错误");
                require(!(Boolean) call(offer, "shouldRewardExp"), "服务端经验奖励开关错误");
                Object data = call(villager, "getVillagerData");
                Object liveLevel = callIfPresent(data, "getLevel");
                if (!(liveLevel instanceof Number)) liveLevel = call(data, "level");
                require(((Number) liveLevel).intValue() == 4, "村民等级没有保存");
                Object profession = callIfPresent(data, "getProfession");
                if (profession == null) profession = call(data, "profession");
                Object type = callIfPresent(data, "getType");
                if (type == null) type = call(data, "type");
                Object professionId = registryEntryId(
                        net.minecraft.core.registries.BuiltInRegistries.VILLAGER_PROFESSION, profession);
                Object typeId = registryEntryId(
                        net.minecraft.core.registries.BuiltInRegistries.VILLAGER_TYPE, type);
                require("minecraft:librarian".equals(professionId),
                        "村民职业没有保存，实际=" + professionId);
                require("minecraft:desert".equals(typeId),
                        "村民类型没有保存，实际=" + typeId);
                return null;
            });
        });
    }

    private static void runScreenLifecycleChecks() {
        check("界面", "物品编辑器六分类真实初始化与渲染", () -> require(itemScreen != null, "物品编辑器未创建"));
        check("界面", "实体编辑器四页签真实初始化与渲染", () -> require(liveEntityScreen != null, "实体编辑器未创建"));
        check("界面", "村民交易编辑器五页签真实初始化与渲染", () -> require(tradeScreen != null, "交易编辑器未创建"));
        check("界面", "窗口缩放后的编辑器重建", () -> onClient(() -> {
            Minecraft mc = Minecraft.getInstance();
            setCurrentScreen(mc, itemScreen);
            resizeScreen(itemScreen, mc,
                    Math.max(640, mc.getWindow().getGuiScaledWidth()),
                    Math.max(360, mc.getWindow().getGuiScaledHeight()));
            return null;
        }));
    }

    private static void resizeScreen(Screen screen, Minecraft mc, int width, int height) throws Exception {
        try {
            call(screen, "resize", mc, width, height);
            return;
        } catch (NoSuchMethodException ignored) {
            // Minecraft 26.2 removes the Minecraft parameter from Screen#resize.
        }
        try {
            call(screen, "resize", width, height);
        } catch (NoSuchMethodException ignored) {
            // Keep a final compatibility path for snapshots exposing only init(width, height).
            call(screen, "init", width, height);
        }
    }

    @SuppressWarnings("unchecked")
    private static void configureTradeDraft(VillagerTradeEditorScreen screen) throws Exception {
        List<Object> trades = (List<Object>) getField(screen, "trades");
        call(screen, "ensureTrades");
        while (trades.size() > 1) trades.remove(trades.size() - 1);
        setField(screen, "tradeIndex", 0);
        Object trade = trades.get(0);
        setField(trade, "buyId", "minecraft:emerald");
        setField(trade, "buyCount", 5);
        setField(trade, "buy2Id", "minecraft:book");
        setField(trade, "buy2Count", 1);
        setField(trade, "sellId", "minecraft:diamond_sword");
        setField(trade, "sellCount", 1);
        setField(trade, "maxUses", 24);
        setField(trade, "uses", 3);
        setField(trade, "xp", 15);
        setField(trade, "specialPrice", -2);
        setField(trade, "demand", 4);
        setField(trade, "priceMultiplier", 0.2F);
        setField(trade, "rewardExp", false);
        setField(trade, "draft", null);
        call(screen, "loadTradeToForm", 0);
    }

    private static int professionIndex(VillagerTradeEditorScreen screen, String id) throws Exception {
        Object value = call(screen, "professionIndexById", id);
        return value instanceof Number number && number.intValue() >= 0 ? number.intValue() : 0;
    }

    private static ItemStack editedStack(SimpleEditorScreen screen) throws Exception {
        return ((ItemStack) getField(screen, "editStack")).copy();
    }

    private static boolean readBoolean(Object screen, CompoundTag tag, String key) throws Exception {
        Object value = call(screen, "readBoolTag", tag, key, false);
        return Boolean.TRUE.equals(value);
    }

    private static Number readNumber(CompoundTag tag, String key) throws Exception {
        Object raw = unwrapOptional(call(tag, "get", key));
        require(raw != null, "NBT字段缺失: " + key);
        for (String method : List.of(
                "getAsInt", "getAsFloat", "getAsDouble", "getAsLong", "getAsShort", "getAsByte",
                "asInt", "asFloat", "asDouble", "asLong", "asShort", "asByte", "asNumber",
                "intValue", "floatValue", "doubleValue", "longValue", "shortValue", "byteValue",
                "box", "value")) {
            try {
                Object value = unwrapOptional(call(raw, method));
                if (value instanceof Number number) return number;
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new AssertionError("NBT字段不是数值: " + key);
    }

    private static String readString(CompoundTag tag, String key) throws Exception {
        Object raw = unwrapOptional(call(tag, "get", key));
        if (raw == null) return "";
        for (String method : List.of("getAsString", "asString")) {
            try {
                Object value = unwrapOptional(call(raw, method));
                if (value != null) return value.toString();
            } catch (NoSuchMethodException ignored) {
            }
        }
        return raw.toString().replace("\"", "");
    }

    private static void setEditBox(Object target, String fieldName, String value) throws Exception {
        Object box = getField(target, fieldName);
        call(box, "setValue", value);
    }

    private static void showAndCycle(Screen screen, String enumField, String capturePrefix, long delayMillis) throws Exception {
        onClient(() -> {
            setCurrentScreen(Minecraft.getInstance(), screen);
            return null;
        });
        sleep(delayMillis * 2L);
        Field field = field(screen.getClass(), enumField);
        Object[] values = field.getType().getEnumConstants();
        require(values != null && values.length > 0, "页签枚举为空: " + enumField);
        for (Object value : values) {
            onClient(() -> {
                field.set(screen, value);
                callIfPresent(screen, "rebuildButtons");
                return null;
            });
            sleep(Math.max(450L, delayMillis * 2L));
            String tabName = String.valueOf(value).toLowerCase(Locale.ROOT);
            check("显示", capturePrefix + " / " + tabName + " 页签渲染", () -> {
                FrameStats stats = onClient(() -> captureFrame(capturePrefix + "-" + tabName, true));
                require(stats.distinctColorBins() >= 16, "截图颜色层次过少，疑似空白或闪屏");
                require(stats.maxLuminance() - stats.minLuminance() >= 40,
                        "截图明暗范围异常，疑似界面未绘制");
                require(!stats.scissorEnabled(), "界面帧结束后裁剪状态仍处于启用状态");
            });
        }
        check("显示", capturePrefix + " 稳定帧无整屏闪烁", () -> {
            FrameStats first = onClient(() -> captureFrame(capturePrefix + "-stability-a", true));
            sleep(140L);
            FrameStats second = onClient(() -> captureFrame(capturePrefix + "-stability-b", true));
            sleep(140L);
            FrameStats third = onClient(() -> captureFrame(capturePrefix + "-stability-c", true));
            require(!first.scissorEnabled() && !second.scissorEnabled() && !third.scissorEnabled(),
                    "连续帧结束后检测到裁剪状态泄漏");
            double largestJump = Math.max(
                    Math.abs(first.meanLuminance() - second.meanLuminance()),
                    Math.abs(second.meanLuminance() - third.meanLuminance()));
            require(largestJump < 45.0, "连续稳定帧亮度突变=" + largestJump + "，疑似整屏闪烁");
        });
        onClient(() -> {
            require(currentScreen(Minecraft.getInstance()) == screen, "编辑器在渲染过程中意外关闭");
            return null;
        });
    }

    private static FrameStats captureFrame(String name, boolean writePng) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();
        require(width > 0 && height > 0, "帧缓冲尺寸无效");
        boolean scissorEnabled = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);

        int staleGlError;
        while ((staleGlError = GL11.glGetError()) != GL11.GL_NO_ERROR) {
            System.err.println("[AnkiNBT QA] 截图前检测到OpenGL错误: " + staleGlError);
        }
        ByteBuffer pixels = BufferUtils.createByteBuffer(Math.multiplyExact(Math.multiplyExact(width, height), 4));
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
        int glError = GL11.glGetError();
        require(glError == GL11.GL_NO_ERROR, "读取帧缓冲失败，OpenGL错误=" + glError);

        int outputWidth = Math.min(640, width);
        int outputHeight = Math.max(1, (int) Math.round(height * (outputWidth / (double) width)));
        BufferedImage image = writePng ? new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_ARGB) : null;
        boolean[] bins = new boolean[4096];
        int distinct = 0;
        int minLuma = 255;
        int maxLuma = 0;
        long lumaTotal = 0L;
        int samples = 0;

        for (int y = 0; y < outputHeight; y++) {
            int sourceY = height - 1 - Math.min(height - 1, (int) ((long) y * height / outputHeight));
            for (int x = 0; x < outputWidth; x++) {
                int sourceX = Math.min(width - 1, (int) ((long) x * width / outputWidth));
                int offset = (sourceY * width + sourceX) * 4;
                int red = pixels.get(offset) & 0xFF;
                int green = pixels.get(offset + 1) & 0xFF;
                int blue = pixels.get(offset + 2) & 0xFF;
                int alpha = pixels.get(offset + 3) & 0xFF;
                int luma = (red * 54 + green * 183 + blue * 19) >> 8;
                minLuma = Math.min(minLuma, luma);
                maxLuma = Math.max(maxLuma, luma);
                lumaTotal += luma;
                samples++;
                int bin = ((red >> 4) << 8) | ((green >> 4) << 4) | (blue >> 4);
                if (!bins[bin]) {
                    bins[bin] = true;
                    distinct++;
                }
                if (image != null) image.setRGB(x, y, (alpha << 24) | (red << 16) | (green << 8) | blue);
            }
        }

        if (image != null) {
            Files.createDirectories(SCREENSHOT_DIR);
            String safeName = name.replaceAll("[^0-9A-Za-z._-]", "_");
            Path output = SCREENSHOT_DIR.resolve(safeName + ".png");
            require(ImageIO.write(image, "png", output.toFile()), "PNG编码器不可用");
            synchronized (CAPTURES) {
                CAPTURES.add(output.toString());
            }
        }
        return new FrameStats(minLuma, maxLuma, distinct,
                lumaTotal / (double) Math.max(1, samples), scissorEnabled);
    }

    private static Entity createEntity(String typeField, double x, double y, double z) throws Exception {
        Entity entity = onServer(() -> {
            ServerLevel level = Minecraft.getInstance().getSingleplayerServer().overworld();
            Object type = resolveEntityType(typeField);
            Entity created = null;
            List<Method> methods = Arrays.stream(type.getClass().getMethods())
                    .filter(method -> method.getName().equals("create"))
                    .sorted((a, b) -> Integer.compare(a.getParameterCount(), b.getParameterCount()))
                    .toList();
            for (Method candidate : methods) {
                try {
                    Object[] args = createArguments(candidate.getParameterTypes(), level);
                    Object value = candidate.invoke(type, args);
                    if (value instanceof Entity found) {
                        created = found;
                        break;
                    }
                } catch (Throwable ignored) {
                }
            }
            require(created != null, "无法创建实体类型 " + typeField);
            // Keep runtime targets in the active player chunk. Absolute test
            // coordinates made the integrated-server lookup nondeterministic
            // when a reused QA world spawned the player far from the origin.
            var players = level.getServer().getPlayerList().getPlayers();
            if (!players.isEmpty()) {
                var player = players.get(0);
                created.setPos(player.getX() + x, player.getY() + 3.0, player.getZ() + z);
            } else {
                created.setPos(x, y, z);
            }
            require(level.addFreshEntity(created), "实体没有加入服务端世界: " + typeField);
            return created;
        });
        synchronized (CREATED_ENTITIES) {
            CREATED_ENTITIES.add(entity);
        }
        return entity;
    }

    private static Object resolveEntityType(String typeField) throws Exception {
        try {
            return EntityType.class.getField(typeField).get(null);
        } catch (NoSuchFieldException ignored) {
        }

        String expectedId = "minecraft:" + typeField.toLowerCase(Locale.ROOT);
        Object registry = net.minecraft.core.registries.BuiltInRegistries.class
                .getField("ENTITY_TYPE")
                .get(null);
        if (registry instanceof Iterable<?> entries) {
            for (Object entry : entries) {
                for (String method : List.of("getKey", "getId")) {
                    Object id = callIfPresent(registry, method, entry);
                    if (id != null && expectedId.equals(id.toString())) return entry;
                }
            }
        }
        throw new NoSuchFieldException(typeField + " / " + expectedId);
    }

    private static Object[] createArguments(Class<?>[] types, ServerLevel level) {
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Class<?> type = types[i];
            if (type.isInstance(level)) args[i] = level;
            else if (type == BlockPos.class) args[i] = new BlockPos(0, 90, 0);
            else if (type.isEnum()) args[i] = enumConstant(type, "COMMAND", "MOB_SUMMONED", "TRIGGERED");
            else if (type == boolean.class || type == Boolean.class) args[i] = false;
            else if (type == int.class || type == Integer.class) args[i] = 0;
            else if (type == float.class || type == Float.class) args[i] = 0F;
            else if (type == double.class || type == Double.class) args[i] = 0D;
            else args[i] = null;
        }
        return args;
    }

    private static Minecraft waitForClient() throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        while (System.nanoTime() < deadline) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) return mc;
            sleep(50L);
        }
        throw new AssertionError("等待 Minecraft 客户端实例超时");
    }

    private static void ensureWorld(Minecraft mc) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIMEOUT_SECONDS);
        boolean requestedCreation = false;
        long readyAfter = System.nanoTime() + TimeUnit.SECONDS.toNanos(8L);
        while (System.nanoTime() < deadline) {
            if (mc.level != null && mc.player != null && mc.getSingleplayerServer() != null) return;
            if (!requestedCreation && System.nanoTime() >= readyAfter && currentScreen(mc) != null) {
                requestedCreation = requestFreshWorld();
            }
            sleep(250L);
        }
        throw new AssertionError("等待单人测试世界超时");
    }

    private static boolean requestFreshWorld() {
        try {
            Boolean opened = onClient(() -> {
                Minecraft mc = Minecraft.getInstance();
                Class<?> type = Class.forName("net.minecraft.client.gui.screens.worldselection.CreateWorldScreen");
                for (Method candidate : type.getDeclaredMethods()) {
                    if (!Modifier.isStatic(candidate.getModifiers())) continue;
                    String name = candidate.getName().toLowerCase(Locale.ROOT);
                    if (!(name.contains("openfresh") || name.contains("createfresh"))) continue;
                    Class<?>[] params = candidate.getParameterTypes();
                    if (params.length != 2 || !params[0].isAssignableFrom(mc.getClass())) continue;
                    Object closeTarget;
                    Screen parent = currentScreen(mc);
                    if (parent != null && params[1].isInstance(parent)) {
                        closeTarget = parent;
                    } else if (Runnable.class.isAssignableFrom(params[1])) {
                        closeTarget = (Runnable) () -> { };
                    } else {
                        continue;
                    }
                    candidate.setAccessible(true);
                    candidate.invoke(null, mc, closeTarget);
                    return true;
                }
                return false;
            });
            if (!Boolean.TRUE.equals(opened)) return false;
            sleep(1200L);
            return Boolean.TRUE.equals(onClient(EditorAutomationRunner::pressCreateWorld));
        } catch (Throwable error) {
            System.err.println("[AnkiNBT QA] 自动创建测试世界尚未就绪: " + describe(rootCause(error)));
            return false;
        }
    }

    private static boolean pressCreateWorld() throws Exception {
        Screen screen = currentScreen(Minecraft.getInstance());
        if (screen == null || !screen.getClass().getName().contains("CreateWorldScreen")) return false;
        for (Class<?> type = screen.getClass(); type != null; type = type.getSuperclass()) {
            for (Method candidate : type.getDeclaredMethods()) {
                String name = candidate.getName().toLowerCase(Locale.ROOT);
                if (candidate.getParameterCount() == 0
                        && (name.equals("oncreate") || name.equals("createworld") || name.equals("createlevel"))) {
                    candidate.setAccessible(true);
                    candidate.invoke(screen);
                    return true;
                }
            }
        }
        Object children = callIfPresent(screen, "children");
        if (children instanceof Iterable<?> iterable) {
            Object fallback = null;
            for (Object child : iterable) {
                if (!child.getClass().getName().toLowerCase(Locale.ROOT).contains("button")) continue;
                fallback = child;
                Object message = callIfPresent(child, "getMessage");
                String label = message == null ? "" : message.toString().toLowerCase(Locale.ROOT);
                if (label.contains("create") || label.contains("创建") || label.contains("建立")) {
                    call(child, "onPress");
                    return true;
                }
            }
            if (fallback != null) {
                call(fallback, "onPress");
                return true;
            }
        }
        return false;
    }

    private static void setCurrentScreen(Minecraft mc, Screen screen) throws Exception {
        try {
            call(mc, "setScreen", screen);
            return;
        } catch (NoSuchMethodException ignored) {
        }
        try {
            call(mc, "setScreenAndShow", screen);
            return;
        } catch (NoSuchMethodException ignored) {
        }
        Object gui = getField(mc, "gui");
        call(gui, "setScreen", screen);
    }

    private static Screen currentScreen(Minecraft mc) throws Exception {
        try {
            Object value = getField(mc, "screen");
            return value instanceof Screen screen ? screen : null;
        } catch (NoSuchFieldException ignored) {
        }
        Object gui = getField(mc, "gui");
        Object value = call(gui, "screen");
        return value instanceof Screen screen ? screen : null;
    }

    private static <T> T onClient(Callable<T> action) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        CompletableFuture<T> future = new CompletableFuture<>();
        mc.execute(() -> {
            try {
                future.complete(action.call());
            } catch (Throwable error) {
                future.completeExceptionally(error);
            }
        });
        return future.get(30L, TimeUnit.SECONDS);
    }

    private static <T> T onServer(Callable<T> action) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        var server = mc.getSingleplayerServer();
        require(server != null, "集成服务器为空");
        CompletableFuture<T> future = new CompletableFuture<>();
        server.execute(() -> {
            try {
                future.complete(action.call());
            } catch (Throwable error) {
                future.completeExceptionally(error);
            }
        });
        return future.get(30L, TimeUnit.SECONDS);
    }

    private static void waitUntil(long timeoutMillis, ThrowingBooleanSupplier condition) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        Throwable last = null;
        while (System.nanoTime() < deadline) {
            try {
                if (condition.getAsBoolean()) return;
            } catch (Throwable error) {
                last = error;
            }
            sleep(100L);
        }
        if (last != null) throw new AssertionError("等待状态生效超时", last);
        throw new AssertionError("等待状态生效超时");
    }

    private static void cleanupEntities() {
        try {
            onServer(() -> {
                synchronized (CREATED_ENTITIES) {
                    for (Entity entity : CREATED_ENTITIES) {
                        if (entity != null && entity.isAlive()) entity.discard();
                    }
                    CREATED_ENTITIES.clear();
                }
                return null;
            });
        } catch (Throwable ignored) {
        }
    }

    private static void stopClient() {
        try {
            sleep(750L);
            onClient(() -> {
                Minecraft mc = Minecraft.getInstance();
                for (String name : List.of("stop", "close")) {
                    try {
                        Method stop = method(mc.getClass(), name, 0, new Object[0]);
                        stop.invoke(mc);
                        return null;
                    } catch (Throwable ignored) {
                    }
                }
                return null;
            });
        } catch (Throwable ignored) {
        }
    }

    private static void check(String category, String name, ThrowingRunnable action) {
        try {
            action.run();
            record(category, name, true, "通过");
        } catch (Throwable error) {
            record(category, name, false, describe(rootCause(error)));
        }
    }

    private static synchronized void record(String category, String name, boolean passed, String detail) {
        RESULTS.add(new Result(category, name, passed, detail == null ? "" : detail));
        writeReport("RUNNING");
        System.out.println("[AnkiNBT QA] " + (passed ? "PASS" : "FAIL") + " | " + category + " | " + name
                + (passed ? "" : " | " + detail));
    }

    private static synchronized void writeReport(String status) {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("schema", 1);
            root.addProperty("loader", LOADER);
            root.addProperty("minecraftVersion", VERSION);
            root.addProperty("language", LANGUAGE);
            root.addProperty("fullscreen", true);
            root.addProperty("guiScale", 4);
            root.addProperty("screenshotDirectory", SCREENSHOT_DIR.toString());
            root.addProperty("status", status);
            root.addProperty("startedAt", STARTED_AT.toString());
            root.addProperty("updatedAt", Instant.now().toString());
            root.addProperty("finished", finished);
            long passed = RESULTS.stream().filter(Result::passed).count();
            root.addProperty("passed", passed);
            root.addProperty("failed", RESULTS.size() - passed);
            root.addProperty("total", RESULTS.size());
            if (fatalError != null) root.addProperty("fatalError", describe(fatalError));
            JsonArray checks = new JsonArray();
            for (Result result : RESULTS) {
                JsonObject check = new JsonObject();
                check.addProperty("category", result.category());
                check.addProperty("name", result.name());
                check.addProperty("passed", result.passed());
                check.addProperty("detail", result.detail());
                checks.add(check);
            }
            root.add("checks", checks);
            JsonArray captures = new JsonArray();
            synchronized (CAPTURES) {
                for (String capture : CAPTURES) captures.add(capture);
            }
            root.add("captures", captures);
            Files.createDirectories(REPORT.getParent());
            Files.writeString(REPORT, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (Throwable error) {
            System.err.println("[AnkiNBT QA] Cannot write report: " + error);
        }
    }

    private static Object call(Object target, String name, Object... args) throws Exception {
        if (target == null) throw new AssertionError("调用目标为空: " + name);
        Method method = method(target.getClass(), name, args.length, args);
        return method.invoke(target, args);
    }

    private static Object callIfPresent(Object target, String name, Object... args) {
        try {
            return call(target, name, args);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method method(Class<?> type, String name, int parameterCount, Object[] args) throws NoSuchMethodException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method candidate : current.getDeclaredMethods()) {
                if (!candidate.getName().equals(name) || candidate.getParameterCount() != parameterCount) continue;
                if (args != null && !argumentsMatch(candidate.getParameterTypes(), args)) continue;
                candidate.setAccessible(true);
                return candidate;
            }
        }
        throw new NoSuchMethodException(type.getName() + "#" + name + "/" + parameterCount);
    }

    private static boolean argumentsMatch(Class<?>[] types, Object[] args) {
        for (int i = 0; i < types.length; i++) {
            if (args[i] == null) continue;
            Class<?> expected = boxed(types[i]);
            if (!expected.isInstance(args[i])) return false;
        }
        return true;
    }

    private static Class<?> boxed(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == char.class) return Character.class;
        return type;
    }

    private static Field field(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(type.getName() + "#" + name);
    }

    private static Object getField(Object target, String name) throws Exception {
        return field(target.getClass(), name).get(target);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        field(target.getClass(), name).set(target, value);
    }

    private static Object enumConstant(Class<?> enumType, String... preferredNames) {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) throw new AssertionError("不是可用枚举: " + enumType.getName());
        for (String preferred : preferredNames) {
            for (Object constant : constants) {
                if (((Enum<?>) constant).name().equalsIgnoreCase(preferred)) return constant;
            }
        }
        return constants[0];
    }

    private static Object unwrapOptional(Object value) {
        Object current = value;
        while (current instanceof Optional<?> optional) current = optional.orElse(null);
        if (current instanceof java.util.OptionalInt optional) return optional.isPresent() ? optional.getAsInt() : null;
        if (current instanceof java.util.OptionalLong optional) return optional.isPresent() ? optional.getAsLong() : null;
        if (current instanceof java.util.OptionalDouble optional) return optional.isPresent() ? optional.getAsDouble() : null;
        return current;
    }

    private static String registryEntryId(Object registry, Object entry) {
        Object raw = unwrapOptional(entry);
        Object holderValue = callIfPresent(raw, "value");
        if (holderValue != null) raw = unwrapOptional(holderValue);
        for (String method : List.of("getKey", "getId")) {
            Object id = callIfPresent(registry, method, raw);
            if (id != null) return String.valueOf(id);
        }
        for (String method : List.of("key", "unwrapKey")) {
            Object key = unwrapOptional(callIfPresent(entry, method));
            Object location = callIfPresent(key, "location");
            if (location != null) return String.valueOf(location);
        }
        Object location = callIfPresent(entry, "location");
        return location == null ? String.valueOf(entry) : String.valueOf(location);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void sleep(long millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    private static Throwable rootCause(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null && current.getCause() != current) current = current.getCause();
        return current;
    }

    private static String describe(Throwable error) {
        if (error == null) return "";
        String message = error.getMessage();
        return error.getClass().getSimpleName() + (message == null || message.isBlank() ? "" : ": " + message);
    }

    private record Result(String category, String name, boolean passed, String detail) {
    }

    private record FrameStats(int minLuminance, int maxLuminance, int distinctColorBins,
                              double meanLuminance, boolean scissorEnabled) {
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    private interface ThrowingBooleanSupplier {
        boolean getAsBoolean() throws Exception;
    }
}
