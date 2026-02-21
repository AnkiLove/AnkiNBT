package com.ankinbt.nbt;

import com.ankinbt.config.AnkiConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public final class NbtFileIO {

    private static final Logger LOGGER = LoggerFactory.getLogger("AnkiNBT");

    private NbtFileIO() {}

    public static Path exportNbt(CompoundTag tag, String fileName, String category, String alias) {
        try {
            Path dir = AnkiConfig.getExportPath(category);
            if (!fileName.endsWith(".nbt")) fileName += ".nbt";
            Path file = dir.resolve(fileName);
            try (OutputStream os = new BufferedOutputStream(Files.newOutputStream(file))) {
                NbtIo.writeCompressed(tag, os);
            }
            // Save alias metadata if provided
            if (alias != null && !alias.isBlank()) {
                saveAlias(dir, fileName, alias.trim());
            }
            if (category != null && !category.isBlank()) {
                AnkiConfig.setLastExportCategory(category.trim());
            }
            AnkiConfig.setLastNbtFile(file.toString());
            LOGGER.info("Exported NBT to {}", file);
            return file;
        } catch (IOException e) {
            LOGGER.warn("Failed to export NBT: {}", e.getMessage());
            return null;
        }
    }

    public static Path exportNbt(CompoundTag tag, String fileName) {
        return exportNbt(tag, fileName, null, null);
    }

    private static void saveAlias(Path dir, String nbtFileName, String alias) {
        Path metaFile = dir.resolve(".aliases.properties");
        Properties props = new Properties();
        if (Files.exists(metaFile)) {
            try (Reader r = Files.newBufferedReader(metaFile)) { props.load(r); } catch (IOException ignored) {}
        }
        props.setProperty(nbtFileName, alias);
        try (Writer w = Files.newBufferedWriter(metaFile)) { props.store(w, "AnkiNBT file aliases"); } catch (IOException ignored) {}
    }

    public static String getAlias(Path dir, String nbtFileName) {
        Path metaFile = dir.resolve(".aliases.properties");
        if (!Files.exists(metaFile)) return null;
        Properties props = new Properties();
        try (Reader r = Files.newBufferedReader(metaFile)) { props.load(r); } catch (IOException ignored) {}
        String val = props.getProperty(nbtFileName);
        return (val != null && !val.isBlank()) ? val : null;
    }

    public static CompoundTag importNbt(Path file) {
        try (InputStream is = new BufferedInputStream(Files.newInputStream(file))) {
            CompoundTag tag = NbtIo.readCompressed(is, net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            AnkiConfig.setLastNbtFile(file.toString());
            LOGGER.info("Imported NBT from {}", file);
            return tag;
        } catch (IOException e) {
            LOGGER.warn("Failed to import NBT: {}", e.getMessage());
            return null;
        }
    }

    public static List<NbtFileEntry> listNbtFiles() {
        return listNbtFiles(null);
    }

    public static List<NbtFileEntry> listNbtFiles(String category) {
        List<NbtFileEntry> entries = new ArrayList<>();
        Path dir = AnkiConfig.getExportPath(category);
        if (!Files.isDirectory(dir)) return entries;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.nbt")) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                long size = Files.size(file);
                long modified = Files.getLastModifiedTime(file).toMillis();
                String alias = getAlias(dir, name);
                entries.add(new NbtFileEntry(name, file, size, modified, alias, category));
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to list NBT files: {}", e.getMessage());
        }
        entries.sort((a, b) -> Long.compare(b.modified, a.modified));
        return entries;
    }

    public static CompoundTag autoLoadLast() {
        if (!AnkiConfig.isAutoLoadLastNbt()) return null;
        String last = AnkiConfig.getLastNbtFile();
        if (last.isEmpty()) return null;
        Path file = Path.of(last);
        if (!Files.exists(file)) return null;
        return importNbt(file);
    }

    public record NbtFileEntry(String name, Path path, long size, long modified, String alias, String category) {
        public String displayName() {
            if (alias != null && !alias.isBlank()) return alias + " (" + name + ")";
            return name;
        }
        public String sizeDisplay() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}
