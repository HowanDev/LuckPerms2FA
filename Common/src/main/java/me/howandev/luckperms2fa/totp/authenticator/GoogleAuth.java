package me.howandev.luckperms2fa.totp.authenticator;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import dev.samstevens.totp.util.Utils;
import me.howandev.luckperms2fa.totp.Authenticator;

import java.util.Locale;

//TODO: add support for arbitrary-length secret, digits & period.
public class GoogleAuth implements Authenticator {
    private final HashingAlgorithm hashingAlgorithm;
    private final int digits;
    private final int period;
    private final TimeProvider timeProvider;
    private final CodeVerifier codeVerifier;

    public GoogleAuth(String algorithm, int digits, int period) {
        this.hashingAlgorithm = HashingAlgorithm.valueOf(algorithm.toUpperCase(Locale.ROOT));
        this.digits = digits;
        this.period = period;

        CodeGenerator generator = new DefaultCodeGenerator(hashingAlgorithm);
        timeProvider = new SystemTimeProvider();
        codeVerifier = new DefaultCodeVerifier(generator, timeProvider);
    }

    @Override
    public String getImplementationName() {
        return "Google Authenticator - samstevens.dev";
    }

    @Override
    public long getTime() {
        return timeProvider.getTime();
    }

    @Override
    public byte[] generatePngQrData(String label, String secret, String issuer) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(label)
                .secret(secret)
                .issuer(issuer)
                .algorithm(hashingAlgorithm)
                .digits(digits)
                .period(period)
                .build();

        QrGenerator generator = new ZxingPngQrGenerator();
        return generator.generate(data);
    }

    @Override
    public String generateQrUriEmbed(String label, String secret, String issuer) throws QrGenerationException {
        return Utils.getDataUriForImage(generatePngQrData(label, secret, issuer), "image/png");
    }

    @Override
    public String generateQrData(String label, String secret, String issuer) {
        return new QrData.Builder()
                .label(label)
                .secret(secret)
                .issuer(issuer)
                .algorithm(hashingAlgorithm)
                .digits(6)
                .period(30)
                .build().getUri();
    }

    @Override
    public boolean isCodeValid(String secret, String code) {
        return codeVerifier.isValidCode(secret, code);
    }
}
