package me.howandev.luckperms2fa.auth;

import me.howandev.luckperms2fa.storage.StorageImplementation;
import me.howandev.luckperms2fa.totp.TotpConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthManagerWithStorage {
    /**
     * A Map which contains all loaded users that may be accessed at any time.
     * This map may contain users that are not saved and/or are pending verification.
     */
    private final Map<@NotNull UUID, @NotNull AuthUser> userCache = new HashMap<>();
    /**
     * A Map which contains all verified users that may be accessed at any time.
     * This map may only contain users that are/have been verified.
     */
    private final Map<@NotNull UUID, @NotNull AuthUser> verifiedUsers = new HashMap<>();
    private final StorageImplementation storage;
    public AuthManagerWithStorage(StorageImplementation storage) {
        this.storage = storage;
    }

    public void init() throws Exception {
        storage.init();
    }

    public void shutdown() throws Exception {
        storage.shutdown();
    }

    /**
     * @return Unmodifiable map reflecting userCache
     */
    public Map<@NotNull UUID, @NotNull AuthUser> getUserCache() {
        return Collections.unmodifiableMap(userCache);
    }

    /**
     * @return Unmodifiable map reflecting verifiedUsers
     */
    public Map<@NotNull UUID, @NotNull AuthUser> getVerifiedUsers() {
        return Collections.unmodifiableMap(verifiedUsers);
    }

    public void addUserToCache(@NotNull AuthUser user) {
        userCache.put(user.getUniqueId(), user);
    }

    public @Nullable AuthUser getUserFromCache(@NotNull UUID uuid) {
        return userCache.get(uuid);
    }

    public void removeUserFromCache(@NotNull UUID uuid) {
        userCache.remove(uuid);
    }

    public void addUserToVerified(@NotNull AuthUser user) {
        verifiedUsers.put(user.getUniqueId(), user);
    }

    public @Nullable AuthUser getUserFromVerified(@NotNull UUID uuid) {
        return verifiedUsers.get(uuid);
    }

    public void removeUserFromVerified(@NotNull UUID uuid) {
        verifiedUsers.remove(uuid);
    }

    public CompletableFuture<@Nullable AuthUser> loadUserFromStorage(UUID uuid) {
        return future(() -> {
            AuthUser cachedUser = getUserFromCache(uuid);
            if (cachedUser != null) return cachedUser;

            AuthUser user = storage.loadUser(uuid);
            if (user != null) addUserToCache(user);

            return user;
        });
    }

    public CompletableFuture<Void> saveUserToStorage(AuthUser authUser) {
        return future(() -> {
            try {
                storage.saveUser(authUser);
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        });
    }

    private <T> CompletableFuture<T> future(Callable<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.call();
            } catch (Exception ex) {
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new CompletionException(ex);
            }
        });
    }

    private CompletableFuture<Void> future(Runnable runnable) {
        return CompletableFuture.runAsync(() -> {
            try {
                runnable.run();
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        });
    }
}
