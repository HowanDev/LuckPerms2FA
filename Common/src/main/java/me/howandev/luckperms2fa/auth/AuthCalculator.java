package me.howandev.luckperms2fa.auth;

import net.luckperms.api.context.ContextCalculator;

public abstract class AuthCalculator<P> implements ContextCalculator<P> {
    protected final AuthManager authManager;
    public AuthCalculator(AuthManager authManager) {
        this.authManager = authManager;
    }
}
