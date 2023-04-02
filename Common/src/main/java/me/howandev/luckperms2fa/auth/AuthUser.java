package me.howandev.luckperms2fa.auth;

import java.util.UUID;

/**
 * AuthUser is an object which holds data for Authenticator
 */
public class AuthUser {
    private final UUID uniqueId;
    private final String secret;
    private boolean creationComplete = false; //TODO: store this in database
    private long lastVerificationTime = 0;
    public AuthUser(UUID uniqueId, String secret) {
        this.uniqueId = uniqueId;
        this.secret = secret;
    }

    public AuthUser(UUID uniqueId, String secret, boolean creationComplete) {
        this.uniqueId = uniqueId;
        this.secret = secret;
        this.creationComplete = creationComplete;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    /**
     * @return shared secret used by the authenticator
     */
    public String getSecret() {
        return secret;
    }

    /**
     * @return true, if user has verified by providing TOTP code, that they have access to an authenticator
     */
    public boolean isCreationComplete() {
        return creationComplete;
    }

    /**
     * @return last time user has provided a valid TOTP code
     */
    public long getLastVerificationTime() {
        return lastVerificationTime;
    }

    public void setLastVerificationTime(long lastVerificationTime) {
        this.lastVerificationTime = lastVerificationTime;
    }
}
