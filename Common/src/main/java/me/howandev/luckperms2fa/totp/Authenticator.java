package me.howandev.luckperms2fa.totp;

import org.apache.commons.codec.binary.Base32;

import java.security.SecureRandom;

public interface Authenticator {
    String getImplementationName();

    /**
     * @return current time used by the authenticator
     */
    long getTime();

    /**
     * @return a un-deterministic base32 encoded string
     */
    default String generateSecret() {
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);
        return new Base32().encodeToString(bytes);
    }

    /**
     * Generates raw data for the QR image
     *
     * @param label QR label, usually user email, name or similar
     * @param secret shared secret
     * @param issuer QR issuer, usually company name
     * @return array of bytes representing parsed data
     * @throws Exception If there was an exception while generating the data
     */
    byte[] generatePngQrData(String label, String secret, String issuer) throws Exception;

    /**
     * Generates data for the QR image and parses a URI ready to be embedded in a browser
     *
     * @param label QR label, usually user email, name or similar
     * @param secret shared secret
     * @param issuer QR issuer, usually company name
     * @return Uri formatted string
     * @throws Exception If there was an exception while generating the Uri
     */
    String generateQrUriEmbed(String label, String secret, String issuer) throws Exception;

    /**
     * Generates data for the QR image and parses a URL ready to be passed to the user
     *
     * @param label QR label, usually user email, name or similar
     * @param secret shared secret
     * @param issuer QR issuer, usually company name
     * @return Url to chart.googleapis.com with data
     * @throws Exception If there was an exception while generating the Url
     */
    String generateQrData(String label, String secret, String issuer) throws Exception;

    /**
     * Checks if provided code for given secret is valid for current time provided by {@link Authenticator#getTime()}
     *
     * @param secret shared secret
     * @param code user code
     * @return true if code valid
     */
    boolean isCodeValid(String secret, String code);
}
