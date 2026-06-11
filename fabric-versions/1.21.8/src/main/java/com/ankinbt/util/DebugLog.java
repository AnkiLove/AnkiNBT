/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package com.ankinbt.util;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DebugLog {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"AnkiNBT");
    private static final int MAX_LINES = 120;
    private static final Deque<LineEntry> LINES = new ArrayDeque<LineEntry>();
    private static Path debugFilePath;

    private DebugLog() {
    }

    public static void info(String message, Object ... args) {
        String line = "[INFO] " + DebugLog.format(message, args);
        DebugLog.append(line);
        if (AnkiConfig.isDebugLogEnabled()) {
            LOGGER.info(message, args);
        }
    }

    public static void warn(String message, Object ... args) {
        String line = "[WARN] " + DebugLog.format(message, args);
        DebugLog.append(line);
        LOGGER.warn(message, args);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static List<String> snapshot() {
        Deque<LineEntry> deque = LINES;
        synchronized (deque) {
            ArrayList<String> out = new ArrayList<String>(LINES.size());
            for (LineEntry entry : LINES) {
                if (entry.count <= 1) {
                    out.add(entry.text);
                    continue;
                }
                out.add(entry.text + " (x" + entry.count + ")");
            }
            return out;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void clear() {
        Deque<LineEntry> deque = LINES;
        synchronized (deque) {
            LINES.clear();
        }
        DebugLog.clearDebugFile();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static void append(String line) {
        Deque<LineEntry> deque = LINES;
        synchronized (deque) {
            LineEntry found = null;
            for (LineEntry entry : LINES) {
                if (!entry.text.equals(line)) continue;
                found = entry;
                break;
            }
            if (found != null) {
                LINES.remove(found);
                ++found.count;
                LINES.addLast(found);
                return;
            }
            LINES.addLast(new LineEntry(line));
            while (LINES.size() > 120) {
                LINES.removeFirst();
            }
        }
        DebugLog.appendToDebugFile(line);
    }

    private static void appendToDebugFile(String line) {
        if (!AnkiConfig.isDebugFileSaveEnabled()) {
            return;
        }
        Path path = DebugLog.resolveDebugFilePath();
        if (path == null) {
            return;
        }
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Files.writeString(path, (CharSequence)(line + System.lineSeparator()), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static void clearDebugFile() {
        Path path = DebugLog.resolveDebugFilePath();
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static Path resolveDebugFilePath() {
        if (debugFilePath != null) {
            return debugFilePath;
        }
        try {
            Path configDir = VersionCompat.get().getConfigDir();
            if (configDir == null) {
                return null;
            }
            debugFilePath = configDir.resolve("ankinbt-debug.log");
            return debugFilePath;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static String format(String pattern, Object ... args) {
        if (pattern == null) {
            return "";
        }
        Object out = pattern;
        if (args != null) {
            for (Object arg : args) {
                int idx = ((String)out).indexOf("{}");
                if (idx < 0) break;
                String val = String.valueOf(arg);
                out = ((String)out).substring(0, idx) + val + ((String)out).substring(idx + 2);
            }
        }
        return out;
    }

    private static final class LineEntry {
        final String text;
        int count;

        LineEntry(String text) {
            this.text = text;
            this.count = 1;
        }
    }
}

