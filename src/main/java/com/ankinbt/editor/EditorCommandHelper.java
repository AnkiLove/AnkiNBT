package com.ankinbt.editor;

import com.ankinbt.util.DebugLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class EditorCommandHelper {
    private static final int NETWORK_COMMAND_LIMIT = 256;

    private EditorCommandHelper() {}

    public static boolean canUseEntityCommand(Minecraft mc) {
        if (mc == null || mc.player == null) return false;

        if (isIntegratedServer(mc)) {
            DebugLog.info("Entity command permission check: true (integrated server)");
            return true;
        }

        try {
            if (mc.player.isCreative()) {
                DebugLog.info("Entity command permission check: true (creative fallback)");
                return true;
            }
        } catch (Throwable ignored) {}

        Object player = mc.player;

        Boolean byPermissionLevel = invokeBool(player, "hasPermissionLevel", 2);
        if (byPermissionLevel != null) {
            DebugLog.info("Entity command permission check by hasPermissionLevel: {}", byPermissionLevel);
            if (byPermissionLevel) return true;
        }

        Integer permissionLevel = invokeInt(player, "getPermissionLevel");
        if (permissionLevel != null) {
            boolean allowed = permissionLevel >= 2;
            DebugLog.info("Entity command permission check by getPermissionLevel({}): {}", permissionLevel, allowed);
            if (allowed) return true;
        }

        DebugLog.info("Entity command permission check: false");
        return false;
    }

    public static boolean applyMergeToEntity(Minecraft mc, Entity entity, CompoundTag mergeTag) {
        if (mc == null || entity == null || mergeTag == null) return false;
        String commandTag = mergeTag.toString();
        String selector = selectorByUuid(entity.getUUID());
        String command = "data merge entity " + selector + " " + commandTag;

        if (tryRunLocalCommand(mc, command)) {
            DebugLog.info("Applied entity merge on integrated server: {}", selector);
            return true;
        }

        if (mc.getConnection() == null) return false;
        if (command.length() > NETWORK_COMMAND_LIMIT) {
            DebugLog.warn("Entity merge command too long for network dispatch: {} chars", command.length());
            return false;
        }

        DebugLog.info("Dispatch entity merge command: {}", command);
        if (sendCommand(mc.getConnection(), command)) return true;
        DebugLog.warn("Failed to dispatch entity merge command for target {}", selector);
        return false;
    }

    public static boolean setEntityMaxHealth(Minecraft mc, Entity entity, float value) {
        if (mc == null || entity == null || value <= 0.0f) return false;
        String selector = selectorByUuid(entity.getUUID());
        String command = "attribute " + selector + " minecraft:max_health base set " + value;

        if (tryRunLocalCommand(mc, command)) return true;
        if (mc.getConnection() == null) return false;
        if (command.length() > NETWORK_COMMAND_LIMIT) {
            DebugLog.warn("Attribute command too long for network dispatch: {} chars", command.length());
            return false;
        }
        return sendCommand(mc.getConnection(), command);
    }

    private static boolean sendCommand(Object connection, String command) {
        Boolean sent = invokeCommand(connection, "sendCommand", command);
        if (sent != null) {
            DebugLog.info("sendCommand result: {}", sent);
            return sent;
        }

        sent = invokeCommand(connection, "sendUnsignedCommand", command);
        if (sent != null) {
            DebugLog.info("sendUnsignedCommand result: {}", sent);
            return sent;
        }
        try {
            Method m = connection.getClass().getMethod("sendCommand", String.class);
            m.invoke(connection, command);
            DebugLog.info("sendCommand invoked without boolean result");
            return true;
        } catch (Throwable ignored) {}

        // Fabric production runs with remapped names; reflection by literal method name is brittle.
        Boolean bySignature = invokeCommandBySignature(connection, command);
        if (bySignature != null) {
            DebugLog.info("sendCommand by signature result: {}", bySignature);
            return bySignature;
        }
        return false;
    }

    public static String selectorByUuid(UUID uuid) {
        int[] arr = uuidToIntArray(uuid);
        return "@e[limit=1,nbt={UUID:[I;" + arr[0] + "," + arr[1] + "," + arr[2] + "," + arr[3] + "]}]";
    }

    public static int[] uuidToIntArray(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        return new int[]{
                (int) (most >> 32),
                (int) most,
                (int) (least >> 32),
                (int) least
        };
    }

    public static Entity findIntegratedServerEntity(Minecraft mc, Entity clientEntity) {
        if (mc == null || clientEntity == null) return null;
        return findIntegratedServerEntity(mc.getSingleplayerServer(), clientEntity.getId(), clientEntity.getUUID());
    }

    public static Entity findIntegratedServerEntity(IntegratedServer server, int entityId, UUID entityUuid) {
        if (server == null || entityUuid == null) return null;
        for (ServerLevel level : server.getAllLevels()) {
            Entity direct = level.getEntity(entityId);
            if (direct != null && entityUuid.equals(direct.getUUID())) return direct;
            for (Entity candidate : level.getAllEntities()) {
                if (entityUuid.equals(candidate.getUUID())) return candidate;
            }
        }
        return null;
    }

    private static Boolean invokeBool(Object target, String method, Object... args) {
        try {
            Class<?>[] types = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i] instanceof Integer ? int.class : args[i].getClass();
            }
            Object out = target.getClass().getMethod(method, types).invoke(target, args);
            return out instanceof Boolean b ? b : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Integer invokeInt(Object target, String method) {
        try {
            Object out = target.getClass().getMethod(method).invoke(target);
            return out instanceof Number n ? n.intValue() : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isIntegratedServer(Minecraft mc) {
        try {
            if (mc.hasSingleplayerServer()) return true;
        } catch (Throwable ignored) {}
        try {
            if (mc.isLocalServer()) return true;
        } catch (Throwable ignored) {}
        try {
            if (mc.getSingleplayerServer() != null) return true;
        } catch (Throwable ignored) {}
        try {
            return mc.getCurrentServer() == null && mc.level != null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean tryRunLocalCommand(Minecraft mc, String command) {
        if (mc == null || command == null || command.isBlank()) return false;
        IntegratedServer server;
        try {
            server = mc.getSingleplayerServer();
        } catch (Throwable ignored) {
            return false;
        }
        if (server == null) return false;

        AtomicBoolean success = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        server.execute(() -> {
            try {
                CommandSourceStack source = server.createCommandSourceStack();
                source = withMaximumPermission(source);
                source = withSuppressedOutput(source);
                server.getCommands().performPrefixedCommand(source, command);
                success.set(true);
            } catch (Throwable t) {
                DebugLog.warn("Integrated server command execution failed: {}", t.toString());
            } finally {
                latch.countDown();
            }
        });

        try {
            if (!latch.await(3, TimeUnit.SECONDS)) {
                DebugLog.warn("Timed out waiting for integrated server command execution");
                return false;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return success.get();
    }

    private static CommandSourceStack withMaximumPermission(CommandSourceStack source) {
        if (source == null) return null;
        try {
            Method m = source.getClass().getMethod("withPermission", int.class);
            Object out = m.invoke(source, 4);
            if (out instanceof CommandSourceStack stack) return stack;
        } catch (Throwable ignored) {}
        try {
            Class<?> permissionSetClass = Class.forName("net.minecraft.server.permissions.PermissionSet");
            Object all = permissionSetClass.getField("ALL_PERMISSIONS").get(null);
            Method withMaximum = source.getClass().getMethod("withMaximumPermission", permissionSetClass);
            Object out = withMaximum.invoke(source, all);
            if (out instanceof CommandSourceStack stack) return stack;
        } catch (Throwable ignored) {}
        try {
            Class<?> permissionSetClass = Class.forName("net.minecraft.server.permissions.PermissionSet");
            Object all = permissionSetClass.getField("ALL_PERMISSIONS").get(null);
            Method withPermission = source.getClass().getMethod("withPermission", permissionSetClass);
            Object out = withPermission.invoke(source, all);
            if (out instanceof CommandSourceStack stack) return stack;
        } catch (Throwable ignored) {}
        return source;
    }

    private static CommandSourceStack withSuppressedOutput(CommandSourceStack source) {
        if (source == null) return null;
        try {
            Method m = source.getClass().getMethod("withSuppressedOutput");
            Object out = m.invoke(source);
            if (out instanceof CommandSourceStack stack) return stack;
        } catch (Throwable ignored) {}
        return source;
    }

    private static Boolean invokeCommand(Object connection, String method, String command) {
        try {
            Method m = connection.getClass().getMethod(method, String.class);
            Object out = m.invoke(connection, command);
            if (out instanceof Boolean b) return b;
            return true;
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private static Boolean invokeCommandBySignature(Object connection, String command) {
        if (connection == null || command == null) return null;
        Method[] methods = connection.getClass().getMethods();
        List<Method> candidates = new ArrayList<>();
        for (Method m : methods) {
            if (Modifier.isStatic(m.getModifiers())) continue;
            Class<?>[] p = m.getParameterTypes();
            if (p.length < 1 || p.length > 2) continue;
            if (p[0] != String.class) continue;
            if (!(m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class || m.getReturnType() == void.class)) continue;
            if (p.length == 2 && !isSupportedSecondParam(p[1])) continue;
            candidates.add(m);
        }
        if (candidates.isEmpty()) return null;

        // Prefer boolean-returning methods, then one-arg methods.
        candidates.sort(Comparator
                .comparingInt((Method m) -> (m.getReturnType() == boolean.class || m.getReturnType() == Boolean.class) ? 0 : 1)
                .thenComparingInt(m -> m.getParameterCount()));

        for (Method m : candidates) {
            try {
                Object out;
                if (m.getParameterCount() == 1) {
                    out = m.invoke(connection, command);
                } else {
                    Object arg1 = secondArgValue(m.getParameterTypes()[1]);
                    if (arg1 == UNSUPPORTED) continue;
                    out = m.invoke(connection, command, arg1);
                }
                if (out instanceof Boolean b) return b;
                return true;
            } catch (Throwable ignored) {}
        }
        return null;
    }

    private static final Object UNSUPPORTED = new Object();

    private static boolean isSupportedSecondParam(Class<?> type) {
        return type == boolean.class
                || type == Boolean.class
                || type == long.class
                || type == Long.class
                || type == Instant.class;
    }

    private static Object secondArgValue(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) return Boolean.TRUE;
        if (type == long.class || type == Long.class) return System.currentTimeMillis();
        if (type == Instant.class) return Instant.now();
        return UNSUPPORTED;
    }
}
