package me.howandev.luckperms2fa.auth;

import me.howandev.luckperms2fa.context.AuthContextKeys;
import me.howandev.luckperms2fa.storage.StorageImplementation;
import me.howandev.luckperms2fa.totp.Authenticator;
import net.luckperms.api.context.Context;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.query.QueryOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthManager {
    /**
     * A Map which contains all online users with context bind that may be accessed at any time.
     * This map may contain users that are not saved and/or are pending verification.
     */
    private final Map<@NotNull UUID, @NotNull AuthUser> userCache = new HashMap<>();
    /**
     * A Map which contains all verified users that may be accessed at any time.
     * This map may only contain users that are/have been verified.
     */
    private final Map<@NotNull UUID, @NotNull AuthUser> verifiedUsers = new HashMap<>();
    private final Authenticator authenticator;
    private final StorageImplementation storage;
    public AuthManager(Authenticator authenticator, StorageImplementation storage) {
        this.authenticator = authenticator;
        this.storage = storage;
    }

    public void initStorage() throws Exception {
        storage.init();
    }

    public void shutdownStorage() throws Exception {
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

    /**
     * adds user to list of cached users
     *
     * @param user authuser to add to list of cachedUsers
     */
    public void addUserToCache(@NotNull AuthUser user) {
        userCache.put(user.getUniqueId(), user);
    }

    /**
     * gets user by uuid from list of cached users
     *
     * @param uuid uuid of user
     */
    public @Nullable AuthUser getUserFromCache(@NotNull UUID uuid) {
        return userCache.get(uuid);
    }

    /**
     * Removes user by uuid from list of cached users
     *
     * @param uuid uuid of user
     */
    public void removeUserFromCache(@NotNull UUID uuid) {
        userCache.remove(uuid);
    }

    /**
     * adds user to list of verified users, this does not update {@link AuthUser#setLastVerificationTime(long)}
     *
     * @param user authuser to add to list of verifiedUsers
     */
    public void addUserToVerified(@NotNull AuthUser user) {
        verifiedUsers.put(user.getUniqueId(), user);
    }

    /**
     * gets user by uuid from list of verified users
     *
     * @param uuid uuid of user
     * @return user, or null if verifiedUsers does not contain anyone by that uuid
     */
    public @Nullable AuthUser getUserFromVerified(@NotNull UUID uuid) {
        return verifiedUsers.get(uuid);
    }

    /**
     * Removes user by uuid from list of verified users
     * 
     * @param uuid uuid of user
     */
    public void removeUserFromVerified(@NotNull UUID uuid) {
        verifiedUsers.remove(uuid);
    }

    /**
     * Check if user authentication is valid.
     * 
     * @param uuid uuid of the user
     * @param timeout period for which authentication is valid
     * @return true if period is still to pass authenticator time
     * 
     * @see Authenticator#isCodeValid(String, String)
     */
    public boolean isAuthValid(UUID uuid, long timeout) {
        AuthUser user = getUserFromCache(uuid);
        if (user == null) return false;

        long verificationTime = user.getLastVerificationTime() + timeout;
        // getTime() will be generally the same as Instant().now().getEpochSecond()
        // though using Authenticator here just in case, if that changes later down the line.
        return authenticator.getTime() <= verificationTime;
    }

    /**
     * Checks if {@link User} has any tracks, groups or permissions with {@code 2fa-verified = true} context
     *
     * @param user luckperms user
     * @return true, if there is a track, group or permission with {@code 2fa-verified = true} context
     */
    public boolean isContextBound(User user) {
        Collection<Node> nodes = user.resolveInheritedNodes(QueryOptions.nonContextual());
        for (Node node : nodes) {
            for (Context context : node.getContexts()) {
                if (context.getKey().equalsIgnoreCase(AuthContextKeys.AUTH_KEY) && context.getValue().equalsIgnoreCase("true")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Explicitly goes to the {@link AuthManager#storage} and attempts to load {@link AuthUser}
     * 
     * @param uuid {@link UUID} of the user
     * @return {@link AuthUser}, or null if not found
     */
    public CompletableFuture<@Nullable AuthUser> loadUserFromStorage(UUID uuid) {
        return future(() -> {
            AuthUser user = storage.loadUser(uuid);
            if (user != null) addUserToCache(user);

            return user;
        });
    }

    /**
     * Attempts to load {@link AuthUser} from {@link AuthManager#userCache}, if not found, tries loading {@link AuthUser} from {@link AuthManager#storage}
     * 
     * @param uuid {@link UUID} of the user
     * @return {@link AuthUser}, or null if not found in both {@link AuthManager#userCache} and {@link AuthManager#storage}
     */
    public CompletableFuture<@Nullable AuthUser> loadUser(UUID uuid) {
        return future(() -> {
            AuthUser cachedUser = getUserFromCache(uuid);
            if (cachedUser != null) return cachedUser;

            return loadUserFromStorage(uuid).get();
        });
    }

    /**
     * Attempts to save {@link AuthUser} to {@link AuthManager#storage}
     *
     * @param authUser {@link AuthUser} to save
     * @return {@link CompletableFuture<Void>}
     */
    public CompletableFuture<Void> saveUserToStorage(AuthUser authUser) {
        return future(() -> {
            try {
                storage.saveUser(authUser);
            } catch (Exception ex) {
                ex.printStackTrace();
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
