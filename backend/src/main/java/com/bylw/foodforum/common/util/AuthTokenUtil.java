package com.bylw.foodforum.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenUtil {

    @Value("${app.auth.token-secret}")
    private String tokenSecret;

    @Value("${app.auth.token-expire-hours}")
    private long tokenExpireHours;

    private byte[] secretBytes;

    @PostConstruct
    public void init() {
        this.secretBytes = tokenSecret.getBytes(StandardCharsets.UTF_8);
    }

    public String createToken(Long userId) {
        long expireAt = Instant.now().plusSeconds(tokenExpireHours * 3600).toEpochMilli();
        String nonce = UUID.randomUUID().toString().replace("-", "");
        String payload = userId + ":" + expireAt + ":" + nonce;
        String signature = sign(payload);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8))
            + "."
            + signature;
    }

    public Long parseUserId(String token) {
        try {
            if (token == null || token.trim().isEmpty() || !token.contains(".")) {
                return null;
            }
            String[] parts = token.split("\\.");
            if (parts.length != 2) {
                return null;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
            String expectedSignature = sign(payload);
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[1].getBytes(StandardCharsets.UTF_8))) {
                return null;
            }
            String[] payloadParts = payload.split(":");
            if (payloadParts.length != 3) {
                return null;
            }
            long expireAt = Long.parseLong(payloadParts[1]);
            if (expireAt < System.currentTimeMillis()) {
                return null;
            }
            return Long.parseLong(payloadParts[0]);
        } catch (Exception exception) {
            return null;
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign token", exception);
        }
    }
}
