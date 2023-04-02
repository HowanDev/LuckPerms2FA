package me.howandev.luckperms2fa.context;

public class AuthContextKeys {

    private AuthContextKeys() {
        throw new AssertionError();
    }

    /**
     * The context key used to denote the subjects 2fa verification.
     */
    public static final String AUTH_KEY = "2fa-verified";
}
