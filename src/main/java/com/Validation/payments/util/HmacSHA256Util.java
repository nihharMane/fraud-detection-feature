package com.Validation.payments.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class HmacSHA256Util {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final String secret;

    public HmacSHA256Util(
            @Value("${hmac.secret}") String secret) {
        this.secret = secret;
    }

    // Used by the application
    public String generateHmac(String jsonInput) {
        return generateHmac(jsonInput, secret);
    }

    // Used when a specific secret is supplied, e.g. tests
    public static String generateHmac(String jsonInput, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            secret.getBytes(StandardCharsets.UTF_8),
                            HMAC_SHA256
                    );

            mac.init(secretKey);

            byte[] rawHmac =
                    mac.doFinal(jsonInput.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(rawHmac);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error while generating HMAC", e);
        }
    }
}