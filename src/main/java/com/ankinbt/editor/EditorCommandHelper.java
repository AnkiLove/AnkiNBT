/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.client.server.IntegratedServer
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 */
package com.ankinbt.editor;

import com.ankinbt.util.DebugLog;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class EditorCommandHelper {
    private static final int NETWORK_COMMAND_LIMIT = 256;
    private static final Object UNSUPPORTED = new Object();

    private EditorCommandHelper() {
    }

    public static boolean canUseEntityCommand(Minecraft mc) {
        Integer permissionLevel;
        if (mc == null || mc.player == null) {
            return false;
        }
        if (EditorCommandHelper.isIntegratedServer(mc)) {
            DebugLog.info("Entity command permission check: true (integrated server)", new Object[0]);
            return true;
        }
        try {
            if (mc.player.isCreative()) {
                DebugLog.info("Entity command permission check: true (creative fallback)", new Object[0]);
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        LocalPlayer player = mc.player;
        Boolean byPermissionLevel = EditorCommandHelper.invokeBool(player, "hasPermissionLevel", 2);
        if (byPermissionLevel != null) {
            DebugLog.info("Entity command permission check by hasPermissionLevel: {}", byPermissionLevel);
            if (byPermissionLevel.booleanValue()) {
                return true;
            }
        }
        if ((permissionLevel = EditorCommandHelper.invokeInt(player, "getPermissionLevel")) != null) {
            boolean allowed = permissionLevel >= 2;
            DebugLog.info("Entity command permission check by getPermissionLevel({}): {}", permissionLevel, allowed);
            if (allowed) {
                return true;
            }
        }
        DebugLog.info("Entity command permission check: false", new Object[0]);
        return false;
    }

    public static boolean applyMergeToEntity(Minecraft mc, Entity entity, CompoundTag mergeTag) {
        if (mc == null || entity == null || mergeTag == null) {
            return false;
        }
        String commandTag = mergeTag.toString();
        String selector = EditorCommandHelper.selectorByUuid(entity.getUUID());
        String command = "data merge entity " + selector + " " + commandTag;
        if (EditorCommandHelper.tryRunLocalCommand(mc, command)) {
            DebugLog.info("Applied entity merge on integrated server: {}", selector);
            return true;
        }
        if (mc.getConnection() == null) {
            return false;
        }
        if (command.length() > 256) {
            DebugLog.warn("Entity merge command too long for network dispatch: {} chars", command.length());
            return false;
        }
        DebugLog.info("Dispatch entity merge command: {}", command);
        if (EditorCommandHelper.sendCommand(mc.getConnection(), command)) {
            return true;
        }
        DebugLog.warn("Failed to dispatch entity merge command for target {}", selector);
        return false;
    }

    public static boolean setEntityMaxHealth(Minecraft mc, Entity entity, float value) {
        if (mc == null || entity == null || value <= 0.0f) {
            return false;
        }
        String selector = EditorCommandHelper.selectorByUuid(entity.getUUID());
        String command = "attribute " + selector + " minecraft:max_health base set " + value;
        if (EditorCommandHelper.tryRunLocalCommand(mc, command)) {
            return true;
        }
        if (mc.getConnection() == null) {
            return false;
        }
        if (command.length() > 256) {
            DebugLog.warn("Attribute command too long for network dispatch: {} chars", command.length());
            return false;
        }
        return EditorCommandHelper.sendCommand(mc.getConnection(), command);
    }

    private static boolean sendCommand(Object connection, String command) {
        Boolean sent = EditorCommandHelper.invokeCommand(connection, "sendCommand", command);
        if (sent != null) {
            DebugLog.info("sendCommand result: {}", sent);
            return sent;
        }
        sent = EditorCommandHelper.invokeCommand(connection, "sendUnsignedCommand", command);
        if (sent != null) {
            DebugLog.info("sendUnsignedCommand result: {}", sent);
            return sent;
        }
        try {
            Method m = connection.getClass().getMethod("sendCommand", String.class);
            m.invoke(connection, command);
            DebugLog.info("sendCommand invoked without boolean result", new Object[0]);
            return true;
        }
        catch (Throwable m) {
            Boolean bySignature = EditorCommandHelper.invokeCommandBySignature(connection, command);
            if (bySignature != null) {
                DebugLog.info("sendCommand by signature result: {}", bySignature);
                return bySignature;
            }
            return false;
        }
    }

    public static String selectorByUuid(UUID uuid) {
        int[] arr = EditorCommandHelper.uuidToIntArray(uuid);
        return "@e[limit=1,nbt={UUID:[I;" + arr[0] + "," + arr[1] + "," + arr[2] + "," + arr[3] + "]}]";
    }

    public static int[] uuidToIntArray(UUID uuid) {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        return new int[]{(int)(most >> 32), (int)most, (int)(least >> 32), (int)least};
    }

    public static Entity findIntegratedServerEntity(Minecraft mc, Entity clientEntity) {
        if (mc == null || clientEntity == null) {
            return null;
        }
        return EditorCommandHelper.findIntegratedServerEntity(mc.getSingleplayerServer(), clientEntity.getId(), clientEntity.getUUID());
    }

    public static Entity findIntegratedServerEntity(IntegratedServer server, int entityId, UUID entityUuid) {
        if (server == null || entityUuid == null) {
            return null;
        }
        for (ServerLevel level : server.getAllLevels()) {
            Entity direct = level.getEntity(entityId);
            if (direct != null && entityUuid.equals(direct.getUUID())) {
                return direct;
            }
            for (Entity candidate : level.getAllEntities()) {
                if (!entityUuid.equals(candidate.getUUID())) continue;
                return candidate;
            }
        }
        return null;
    }

    private static Boolean invokeBool(Object target, String method, Object ... args) {
        try {
            Boolean b;
            Class[] types = new Class[args.length];
            for (int i = 0; i < args.length; ++i) {
                types[i] = args[i] instanceof Integer ? Integer.TYPE : args[i].getClass();
            }
            Object out = target.getClass().getMethod(method, types).invoke(target, args);
            return out instanceof Boolean ? (b = (Boolean)out) : null;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static Integer invokeInt(Object target, String method) {
        try {
            Integer n;
            Object out = target.getClass().getMethod(method, new Class[0]).invoke(target, new Object[0]);
            if (out instanceof Number) {
                Number n2 = (Number)out;
                n = n2.intValue();
            } else {
                n = null;
            }
            return n;
        }
        catch (Throwable ignored) {
            return null;
        }
    }

    private static boolean isIntegratedServer(Minecraft mc) {
        try {
            if (mc.hasSingleplayerServer()) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            if (mc.isLocalServer()) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            if (mc.getSingleplayerServer() != null) {
                return true;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            return mc.getCurrentServer() == null && mc.level != null;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private static boolean tryRunLocalCommand(Minecraft mc, String command) {
        IntegratedServer server;
        if (mc == null || command == null || command.isBlank()) {
            return false;
        }
        try {
            server = mc.getSingleplayerServer();
        }
        catch (Throwable ignored) {
            return false;
        }
        if (server == null) {
            return false;
        }
        AtomicBoolean success = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        server.execute(() -> {
            try {
                CommandSourceStack source = server.createCommandSourceStack();
                source = EditorCommandHelper.withMaximumPermission(source);
                source = EditorCommandHelper.withSuppressedOutput(source);
                server.getCommands().performPrefixedCommand(source, command);
                success.set(true);
            }
            catch (Throwable t) {
                DebugLog.warn("Integrated server command execution failed: {}", t.toString());
            }
            finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(3L, TimeUnit.SECONDS)) {
                DebugLog.warn("Timed out waiting for integrated server command execution", new Object[0]);
                return false;
            }
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return success.get();
    }

    private static CommandSourceStack withMaximumPermission(CommandSourceStack source) {
        Object out;
        Object all;
        Class<?> permissionSetClass2;
        if (source == null) {
            return null;
        }
        try {
            Method m = source.getClass().getMethod("withPermission", Integer.TYPE);
            Object out2 = m.invoke(source, 4);
            if (out2 instanceof CommandSourceStack) {
                CommandSourceStack stack = (CommandSourceStack)out2;
                return stack;
            }
        }
        catch (Throwable m) {
            // empty catch block
        }
        try {
            permissionSetClass2 = Class.forName("net.minecraft.server.permissions.PermissionSet");
            all = permissionSetClass2.getField("ALL_PERMISSIONS").get(null);
            Method withMaximum = source.getClass().getMethod("withMaximumPermission", permissionSetClass2);
            out = withMaximum.invoke(source, all);
            if (out instanceof CommandSourceStack) {
                CommandSourceStack stack = (CommandSourceStack)out;
                return stack;
            }
        }
        catch (Throwable t) {
            // empty catch block
        }
        try {
            permissionSetClass2 = Class.forName("net.minecraft.server.permissions.PermissionSet");
            all = permissionSetClass2.getField("ALL_PERMISSIONS").get(null);
            Method withPermission = source.getClass().getMethod("withPermission", permissionSetClass2);
            out = withPermission.invoke(source, all);
            if (out instanceof CommandSourceStack) {
                CommandSourceStack stack = (CommandSourceStack)out;
                return stack;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return source;
    }

    private static CommandSourceStack withSuppressedOutput(CommandSourceStack source) {
        if (source == null) {
            return null;
        }
        try {
            Method m = source.getClass().getMethod("withSuppressedOutput", new Class[0]);
            Object out = m.invoke(source, new Object[0]);
            if (out instanceof CommandSourceStack) {
                CommandSourceStack stack = (CommandSourceStack)out;
                return stack;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return source;
    }

    private static Boolean invokeCommand(Object connection, String method, String command) {
        try {
            Method m = connection.getClass().getMethod(method, String.class);
            Object out = m.invoke(connection, command);
            if (out instanceof Boolean) {
                Boolean b = (Boolean)out;
                return b;
            }
            return true;
        }
        catch (NoSuchMethodException e) {
            return null;
        }
        catch (Throwable ignored) {
            return false;
        }
    }

    private static Boolean invokeCommandBySignature(Object connection, String command) {
        if (connection == null || command == null) {
            return null;
        }
        Method[] methods = connection.getClass().getMethods();
        ArrayList<Method> candidates = new ArrayList<Method>();
        for (Method m2 : methods) {
            Class<?>[] p;
            if (Modifier.isStatic(m2.getModifiers()) || (p = m2.getParameterTypes()).length < 1 || p.length > 2 || p[0] != String.class || m2.getReturnType() != Boolean.TYPE && m2.getReturnType() != Boolean.class && m2.getReturnType() != Void.TYPE || p.length == 2 && !EditorCommandHelper.isSupportedSecondParam(p[1])) continue;
            candidates.add(m2);
        }
        if (candidates.isEmpty()) {
            return null;
        }
        candidates.sort(Comparator.<Method>comparingInt(m -> m.getReturnType() == Boolean.TYPE || m.getReturnType() == Boolean.class ? 0 : 1).thenComparingInt(Method::getParameterCount));
        for (Method m3 : candidates) {
            try {
                Object out;
                if (m3.getParameterCount() == 1) {
                    out = m3.invoke(connection, command);
                } else {
                    Object arg1 = EditorCommandHelper.secondArgValue(m3.getParameterTypes()[1]);
                    if (arg1 == UNSUPPORTED) continue;
                    out = m3.invoke(connection, command, arg1);
                }
                if (out instanceof Boolean) {
                    Boolean b = (Boolean)out;
                    return b;
                }
                return true;
            }
            catch (Throwable throwable) {
            }
        }
        return null;
    }

    private static boolean isSupportedSecondParam(Class<?> type) {
        return type == Boolean.TYPE || type == Boolean.class || type == Long.TYPE || type == Long.class || type == Instant.class;
    }

    private static Object secondArgValue(Class<?> type) {
        if (type == Boolean.TYPE || type == Boolean.class) {
            return Boolean.TRUE;
        }
        if (type == Long.TYPE || type == Long.class) {
            return System.currentTimeMillis();
        }
        if (type == Instant.class) {
            return Instant.now();
        }
        return UNSUPPORTED;
    }
}
