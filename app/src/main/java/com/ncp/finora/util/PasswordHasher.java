package com.ncp.finora.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * SHA-256 dengan salt acak per pengguna. Disimpan di kolom password_hash dengan format
 * "salt$hash" (hex), sehingga skema database tidak perlu diubah.
 */
public final class PasswordHasher {
    private PasswordHasher() { }

    public static String hash(String password) {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        String saltHex = toHex(salt);
        return saltHex + "$" + sha256(saltHex + password);
    }

    public static boolean verify(String password, String stored) {
        int sep = stored.indexOf('$');
        if (sep < 0) return false;
        String saltHex = stored.substring(0, sep);
        String expected = stored.substring(sep + 1);
        return MessageDigest.isEqual(
                sha256(saltHex + password).getBytes(StandardCharsets.UTF_8),
                expected.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
