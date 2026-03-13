package com.open.spring.mvc.person.Email;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResetCode {
    private static final Logger logger = LoggerFactory.getLogger(ResetCode.class);

    private static final long TOKEN_TTL_SECONDS = 5 * 60;
    private static final long RATE_WINDOW_SECONDS = 15 * 60;
    private static final int MAX_REQUESTS_PER_WINDOW = 3;

    private static final SecureRandom random = new SecureRandom();
    private static final Map<String, ResetTokenRecord> activeTokensByUid = new ConcurrentHashMap<>();
    private static final Map<String, Deque<Long>> resetRequestTimesByUid = new ConcurrentHashMap<>();
    private static final Map<String, String> lastIssueReasonByUid = new ConcurrentHashMap<>();

    private static final byte[] secret = loadSecret();

    private static class ResetTokenRecord {
        private final String token;
        private final long expiresAtEpoch;

        private ResetTokenRecord(String token, long expiresAtEpoch) {
            this.token = token;
            this.expiresAtEpoch = expiresAtEpoch;
        }
    }

    private static byte[] loadSecret() {
        String envSecret = System.getenv("RESET_TOKEN_SECRET");
        if (envSecret != null && !envSecret.isBlank()) {
            return envSecret.getBytes(StandardCharsets.UTF_8);
        }

        byte[] generated = new byte[32];
        random.nextBytes(generated);
        logger.warn("AUDIT reset_secret_fallback using ephemeral in-memory secret because RESET_TOKEN_SECRET is not set");
        return generated;
    }

    private static String base64Url(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private static String hmacSha256(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return base64Url(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Unable to sign reset token", e);
        }
    }

    private static void cleanupUidState(String uid) {
        long now = Instant.now().getEpochSecond();

        ResetTokenRecord tokenRecord = activeTokensByUid.get(uid);
        if (tokenRecord != null && tokenRecord.expiresAtEpoch <= now) {
            activeTokensByUid.remove(uid);
            logger.info("AUDIT reset_token_expired uid={}", uid);
        }

        Deque<Long> requestTimes = resetRequestTimesByUid.computeIfAbsent(uid, key -> new ArrayDeque<>());
        while (!requestTimes.isEmpty() && requestTimes.peekFirst() <= now - RATE_WINDOW_SECONDS) {
            requestTimes.removeFirst();
        }
    }

    public static synchronized boolean canIssueResetCode(String uid) {
        cleanupUidState(uid);

        if (activeTokensByUid.containsKey(uid)) {
            lastIssueReasonByUid.put(uid, "active-token");
            return false;
        }

        Deque<Long> requestTimes = resetRequestTimesByUid.computeIfAbsent(uid, key -> new ArrayDeque<>());
        if (requestTimes.size() >= MAX_REQUESTS_PER_WINDOW) {
            lastIssueReasonByUid.put(uid, "rate-limit");
            return false;
        }

        lastIssueReasonByUid.remove(uid);
        return true;
    }

    public static String getLastIssueReason(String uid) {
        return lastIssueReasonByUid.get(uid);
    }

    public static synchronized String GenerateResetCode(String uid){
        if (!canIssueResetCode(uid)) {
            logger.warn("AUDIT reset_token_issue_blocked uid={} reason={}", uid, getLastIssueReason(uid));
            return null;
        }

        long now = Instant.now().getEpochSecond();
        long expiresAt = now + TOKEN_TTL_SECONDS;
        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        String nonceBase64 = base64Url(nonce);

        String payload = uid + "." + expiresAt + "." + nonceBase64;
        String signature = hmacSha256(payload);
        String token = base64Url(uid.getBytes(StandardCharsets.UTF_8)) + "." + expiresAt + "." + nonceBase64 + "." + signature;

        activeTokensByUid.put(uid, new ResetTokenRecord(token, expiresAt));
        resetRequestTimesByUid.computeIfAbsent(uid, key -> new ArrayDeque<>()).addLast(now);
        lastIssueReasonByUid.remove(uid);

        logger.info("AUDIT reset_token_issued uid={} expiresAt={}", uid, expiresAt);
        return token;
    }

    public static synchronized String getCodeForUid(String uid){
        cleanupUidState(uid);
        ResetTokenRecord tokenRecord = activeTokensByUid.get(uid);
        return tokenRecord == null ? null : tokenRecord.token;
    }

    public static synchronized boolean validateAndConsume(String uid, String token) {
        cleanupUidState(uid);
        if (token == null || token.isBlank()) {
            logger.warn("AUDIT reset_token_validate_failed uid={} reason=missing_token", uid);
            return false;
        }

        ResetTokenRecord tokenRecord = activeTokensByUid.get(uid);
        if (tokenRecord == null) {
            logger.warn("AUDIT reset_token_validate_failed uid={} reason=no_active_token", uid);
            return false;
        }

        if (!tokenRecord.token.equals(token)) {
            logger.warn("AUDIT reset_token_validate_failed uid={} reason=token_mismatch", uid);
            return false;
        }

        String[] parts = token.split("\\.");
        if (parts.length != 4) {
            logger.warn("AUDIT reset_token_validate_failed uid={} reason=malformed_token", uid);
            return false;
        }

        long exp;
        try {
            exp = Long.parseLong(parts[1]);
        } catch (NumberFormatException ex) {
            logger.warn("AUDIT reset_token_validate_failed uid={} reason=invalid_exp", uid);
            return false;
        }

        String payload = uid + "." + exp + "." + parts[2];
        String expectedSig = hmacSha256(payload);
        if (!expectedSig.equals(parts[3])) {
            logger.warn("AUDIT reset_token_validate_failed uid={} reason=bad_signature", uid);
            return false;
        }

        if (Instant.now().getEpochSecond() > exp) {
            activeTokensByUid.remove(uid);
            logger.warn("AUDIT reset_token_validate_failed uid={} reason=expired", uid);
            return false;
        }

        activeTokensByUid.remove(uid); // single-use enforcement
        logger.info("AUDIT reset_token_consumed uid={}", uid);
        return true;
    }

    public static synchronized void removeCodeByUid(String uid){
        activeTokensByUid.remove(uid);
        logger.info("AUDIT reset_token_removed uid={}", uid);
    }
}
