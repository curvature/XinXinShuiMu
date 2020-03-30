package net.newsmth.dirac.util;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {

    public static String md5(byte[] bytes) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(bytes);
            byte[] messageDigest = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
        }
        return "";
    }

    public static String encode(String input) {
        byte[] plain = input.getBytes(StandardCharsets.UTF_8);
        xor(plain);
        return Base64.encodeToString(plain, Base64.NO_WRAP);
    }

    public static String decode(String input) {
        if (input == null) {
            return null;
        }
        byte[] plain = Base64.decode(input, Base64.NO_WRAP);
        xor(plain);
        return new String(plain, StandardCharsets.UTF_8);
    }

    private static void xor(byte[] input) {
        for (int i = input.length - 1; i >= 0; --i) {
            input[i] ^= 0x6F;
        }
    }
}
