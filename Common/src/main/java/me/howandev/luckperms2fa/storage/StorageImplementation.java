package me.howandev.luckperms2fa.storage;

import me.howandev.luckperms2fa.auth.AuthUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface StorageImplementation {
    /**
     * Initializes and creates all necessary files for the implementation.
     *
     * @throws Exception If there was an exception while initializing the storage
     */
    void init() throws Exception;

    /**
     * Shutdowns, and saves all cached objects stored by the implementation
     *
     * @throws Exception If there was an exception while shutting down the storage
     */
    void shutdown() throws Exception;

    /**
     * Loads data for the user with provided UUID
     *
     * @param uuid uuid of the user
     * @return user, or null if there is no data stored for provided UUID
     * @throws Exception If there was an exception while getting user data
     */
    @Nullable AuthUser loadUser(UUID uuid) throws Exception;

    /**
     * Saves data for the user
     *
     * @param authUser user to save
     * @throws Exception If there was an exception while getting user data
     */
    void saveUser(@NotNull AuthUser authUser) throws Exception;
}
