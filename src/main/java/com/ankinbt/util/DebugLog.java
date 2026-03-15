package com.ankinbt.util;

import com.ankinbt.compat.VersionCompat;
import com.ankinbt.config.AnkiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class DebugLog {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnkiNBT");
    private static final int MAX_LINES = 120;
    private static final Deque<LineEntry> LINES = new ArrayDeque<>();
    private static Path debugFilePath;

    private DebugLog() {}

    public static void info(String message, Object... args) {
        String line = "[INFO] " + format(message, args);
        append(line);
        if (AnkiConfig.isDebugLogEnabled()) LOGGER.info(message, args);
    }

    public static void warn(String message, Object... args) {
        String line = "[WARN] " + format(message, args);
        append(line);
        // Warnings should always be visible in latest.log for cross-loader diagnostics.
        LOGGER.warn(message, args);
    }

    public static List<String> snapshot() {
        synchronized (LINES) {
            List<String> out = new ArrayList<>(LINES.size());
            for (LineEntry entry : LINES) {
                if (entry.count <= 1) out.add(entry.text);
                else out.add(entry.text + " (x" + entry.count + ")");
            }
            return out;
        }
    }

    public static void clear() {
        synchronized (LINES) {
            LINES.clear();
        }
        clearDebugFile();
    }

    private static void append(String line) {
        synchronized (LINES) {
            LineEntry found = null;
            for (LineEntry entry : LINES) {
                if (entry.text.equals(line)) {
                    found = entry;
                    break;
                }
            }
            if (found != null) {
                LINES.remove(found);
                found.count++;
                LINES.addLast(found);
                return;
            }
            LINES.addLast(new LineEntry(line));
            while (LINES.size() > MAX_LINES) LINES.removeFirst();
        }
        appendToDebugFile(line);
    }

    private static void appendToDebugFile(String line) {
        if (!AnkiConfig.isDebugFileSaveEnabled()) return;
        Path path = resolveDebugFilePath();
        if (path == null) return;
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, line + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (Throwable ignored) {}
    }

    private static void clearDebugFile() {
        Path path = resolveDebugFilePath();
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (Throwable ignored) {}
    }

    private static Path resolveDebugFilePath() {
        if (debugFilePath != null) return debugFilePath;
        try {
            Path configDir = VersionCompat.get().getConfigDir();
            if (configDir == null) return null;
            debugFilePath = configDir.resolve("ankinbt-debug.log");
            return debugFilePath;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String format(String pattern, Object... args) {
        if (pattern == null) return "";
        String out = pattern;
        if (args != null) {
            for (Object arg : args) {
                int idx = out.indexOf("{}");
                if (idx < 0) break;
                String val = String.valueOf(arg);
                out = out.substring(0, idx) + val + out.substring(idx + 2);
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
