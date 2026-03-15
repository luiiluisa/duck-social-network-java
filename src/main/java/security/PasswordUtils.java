package security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class PasswordUtils {

    private static final SecureRandom RNG = new SecureRandom();

    private PasswordUtils() {}

    public static String generateSalt() {
        byte[] salt = new byte[16];
        RNG.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }


    public static String hashPassword(String rawPassword, String salt) {
        if (rawPassword == null) rawPassword = "";
        if (salt == null) salt = "";

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException("Could not hash password", e);
        }
    }


    public static String makeStoredPassword(String rawPassword) {
        String salt = generateSalt();
        String hash = hashPassword(rawPassword, salt);
        return salt + ":" + hash;
    }

    public static boolean verifyPassword(String rawPassword, String stored) {
        if (stored == null || !stored.contains(":")) return false;
        String[] parts = stored.split(":", 2);
        String salt = parts[0];
        String hash = parts[1];
        String computed = hashPassword(rawPassword, salt);
        return computed.equals(hash);
    }
}
