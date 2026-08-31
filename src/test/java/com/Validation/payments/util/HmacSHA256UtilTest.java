package com.Validation.payments.util;

import com.Validation.payments.pojo.PaymentRequest;
import com.Validation.payments.service.TestDataBuilder;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class HmacSHA256UtilTest {

    @Test
    void generateHmac_matchesStandardMacImplementation() throws Exception {

        // Build request object
        PaymentRequest requestObj = TestDataBuilder.buildRequest();

        // 🔥 Use SAME JsonUtil logic
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);

        JsonUtil jsonUtil = new JsonUtil(objectMapper);

        String json = jsonUtil.convertObjectToJson(requestObj);

        String secret = "my-secret-key";

        String expected = computeHmacWithMac(json, secret);
        String actual = HmacSHA256Util.generateHmac(json, secret);

        System.out.println("JSON      : " + json);
        System.out.println("Expected  : " + expected);
        System.out.println("Generated : " + actual);

        assertEquals(expected, actual);
    }

    @Test
    void generateHmac_handlesEmptyStrings() {
        String data = "";
        String secret = "";

        assertThrows(RuntimeException.class,
                () -> HmacSHA256Util.generateHmac(data, secret),
                "Generating HMAC with an empty secret should throw a RuntimeException");
    }

    private String computeHmacWithMac(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(secretKey);
        byte[] raw = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(raw);
    }

    @Test
    void generateHmac_withMultipleDummyValues() {

        String[][] cases = new String[][]{
                {"data1", "secret1"},
                {"The quick brown fox", "s3cr3t!"},
                {"こんにちは", "秘密"},
                {"special-chars-#$%_+@", "p@$$w0rd"},
        };

        for (String[] c : cases) {
            String data = c[0];
            String secret = c[1];

            String expected;
            try {
                expected = computeHmacWithMac(data, secret);
            } catch (Exception e) {
                fail("Unexpected exception computing expected hmac for case ("
                        + data + "," + secret + "): " + e.getMessage());
                return;
            }

            String actual = HmacSHA256Util.generateHmac(data, secret);

            assertEquals(expected, actual,
                    "HMAC mismatch for data='" + data + "' secret='" + secret + "'");
        }
    }

    @Test
    void generateHmac_dummyValues_returnsNonEmpty() {

        String[][] cases = new String[][]{
                {"dummy-data-1", "dummy-secret-1"},
                {"another-data", "another-secret"},
                {"emoji-😀-data", "secret-🔑"},
        };

        for (String[] c : cases) {
            String data = c[0];
            String secret = c[1];

            String result = HmacSHA256Util.generateHmac(data, secret);

            assertNotNull(result,
                    "HMAC result should not be null for data='" + data + "'");
            assertFalse(result.isEmpty(),
                    "HMAC result should not be empty for data='" + data + "'");
        }
    }
}