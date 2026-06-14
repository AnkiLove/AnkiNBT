/*
 * Decompiled with CFR 0.152.
 *
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.NbtAccounter
 *  net.minecraft.nbt.NbtIo
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.ankinbt.nbt;

import com.ankinbt.config.AnkiConfig;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NbtFileIO {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"AnkiNBT");

    private NbtFileIO() {
    }

    public static Path exportNbt(CompoundTag tag, String fileName, String category, String alias) {
        try {
            Path dir = AnkiConfig.getExportPath(category);
            if (!((String)fileName).endsWith(".nbt")) {
                fileName = (String)fileName + ".nbt";
            }
            Path file = dir.resolve((String)fileName);
            try (BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(file, new OpenOption[0]));){
                NbtIo.writeCompressed((CompoundTag)tag, (OutputStream)os);
            }
            if (alias != null && !alias.isBlank()) {
                NbtFileIO.saveAlias(dir, (String)fileName, alias.trim());
            }
            if (category != null && !category.isBlank()) {
                AnkiConfig.setLastExportCategory(category.trim());
            }
            AnkiConfig.setLastNbtFile(file.toString());
            if (AnkiConfig.isDebugLogEnabled()) {
                LOGGER.info("Exported NBT to {}", (Object)file);
            }
            return file;
        }
        catch (IOException e) {
            LOGGER.warn("Failed to export NBT: {}", (Object)e.getMessage());
            return null;
        }
    }

    public static Path exportNbtToPath(CompoundTag tag, Path file, String alias) {
        if (tag == null || file == null) {
            return null;
        }
        try {
            Path parent;
            Path out = file;
            if (!out.getFileName().toString().toLowerCase().endsWith(".nbt")) {
                out = out.resolveSibling(out.getFileName().toString() + ".nbt");
            }
            if ((parent = out.getParent()) != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            try (BufferedOutputStream os = new BufferedOutputStream(Files.newOutputStream(out, new OpenOption[0]));){
                NbtIo.writeCompressed((CompoundTag)tag, (OutputStream)os);
            }
            if (alias != null && !alias.isBlank() && parent != null) {
                NbtFileIO.saveAlias(parent, out.getFileName().toString(), alias.trim());
            }
            AnkiConfig.setLastNbtFile(out.toString());
            if (AnkiConfig.isDebugLogEnabled()) {
                LOGGER.info("Exported NBT to {}", (Object)out);
            }
            return out;
        }
        catch (IOException e) {
            LOGGER.warn("Failed to export NBT to custom path: {}", (Object)e.getMessage());
            return null;
        }
    }

    public static Path exportNbtToPath(CompoundTag tag, Path file) {
        return NbtFileIO.exportNbtToPath(tag, file, null);
    }

    public static Path exportNbt(CompoundTag tag, String fileName) {
        return NbtFileIO.exportNbt(tag, fileName, null, null);
    }

    private static void saveAlias(Path dir, String nbtFileName, String alias) {
        Path metaFile = dir.resolve(".aliases.properties");
        Properties props = new Properties();
        if (Files.exists(metaFile, new LinkOption[0])) {
            try (BufferedReader r2 = Files.newBufferedReader(metaFile);){
                props.load(r2);
            }
            catch (IOException r2) {
                // empty catch block
            }
        }
        props.setProperty(nbtFileName, alias);
        try (BufferedWriter w = Files.newBufferedWriter(metaFile, new OpenOption[0]);){
            props.store(w, "AnkiNBT file aliases");
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public static String getAlias(Path dir, String nbtFileName) {
        Path metaFile = dir.resolve(".aliases.properties");
        if (!Files.exists(metaFile, new LinkOption[0])) {
            return null;
        }
        Properties props = new Properties();
        try (BufferedReader r2 = Files.newBufferedReader(metaFile);){
            props.load(r2);
        }
        catch (IOException r2) {
            // empty catch block
        }
        String val = props.getProperty(nbtFileName);
        return val != null && !val.isBlank() ? val : null;
    }

    public static CompoundTag importNbt(Path file) {
        try (BufferedInputStream is = new BufferedInputStream(Files.newInputStream(file, new OpenOption[0]))) {
            CompoundTag tag = NbtIo.readCompressed((InputStream)is, (NbtAccounter)NbtAccounter.unlimitedHeap());
            AnkiConfig.setLastNbtFile(file.toString());
            if (AnkiConfig.isDebugLogEnabled()) {
                LOGGER.info("Imported NBT from {}", (Object)file);
            }
            return tag;
        } catch (IOException e) {
            LOGGER.warn("Failed to import NBT: {}", (Object)e.getMessage());
            return null;
        }
    }

    public static List<NbtFileEntry> listNbtFiles() {
        return NbtFileIO.listNbtFiles(null);
    }

    public static List<NbtFileEntry> listNbtFiles(String category) {
        ArrayList<NbtFileEntry> entries = new ArrayList<NbtFileEntry>();
        Path dir = AnkiConfig.getExportPath(category);
        if (!Files.isDirectory(dir, new LinkOption[0])) {
            return entries;
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.nbt");){
            for (Path file : stream) {
                String name = file.getFileName().toString();
                long size = Files.size(file);
                long modified = Files.getLastModifiedTime(file, new LinkOption[0]).toMillis();
                String alias = NbtFileIO.getAlias(dir, name);
                entries.add(new NbtFileEntry(name, file, size, modified, alias, category));
            }
        }
        catch (IOException e) {
            LOGGER.warn("Failed to list NBT files: {}", (Object)e.getMessage());
        }
        entries.sort((a, b) -> Long.compare(b.modified, a.modified));
        return entries;
    }

    public static CompoundTag autoLoadLast() {
        if (!AnkiConfig.isAutoLoadLastNbt()) {
            return null;
        }
        String last = AnkiConfig.getLastNbtFile();
        if (last.isEmpty()) {
            return null;
        }
        Path file = Path.of(last, new String[0]);
        if (!Files.exists(file, new LinkOption[0])) {
            return null;
        }
        return NbtFileIO.importNbt(file);
    }

    public record NbtFileEntry(String name, Path path, long size, long modified, String alias, String category) {
        public String displayName() {
            if (this.alias != null && !this.alias.isBlank()) {
                return this.alias + " (" + this.name + ")";
            }
            return this.name;
        }

        public String sizeDisplay() {
            if (this.size < 1024L) {
                return this.size + " B";
            }
            if (this.size < 0x100000L) {
                return String.format("%.1f KB", (double)this.size / 1024.0);
            }
            return String.format("%.1f MB", (double)this.size / 1048576.0);
        }
    }
}
