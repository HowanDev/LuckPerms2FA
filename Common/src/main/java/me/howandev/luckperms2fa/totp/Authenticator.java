package me.howandev.luckperms2fa.totp;

import org.apache.commons.codec.binary.Base32;

import java.security.SecureRandom;

public interface AuthImplementation {
    String getImplementationName();
    long getTime();
    default String generateSecret() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        return new Base32().encodeToString(bytes);
    }
    String generateQrUri(String label, String secret, String issuer);
    boolean isCodeValid(String secret, String code);
}
