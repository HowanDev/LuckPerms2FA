package me.howandev.luckperms2fa.totp.authenticator;

import me.howandev.luckperms2fa.totp.AuthenticatorImplementation;
import org.apache.commons.codec.binary.Base32;

import java.security.SecureRandom;

public class GoogleAuthenticator implements AuthenticatorImplementation {
    @Override
    public String generateSecret() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        return new Base32().encodeToString(bytes);
    }

    @Override
    public String generateQrUri() {
        return null;
    }

    @Override
    public boolean isCodeValid(String secret, String code) {
        return false;
    }
}
