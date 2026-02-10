package com.org.application.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class Hashing {

    public static String sha(String s) {
        try {
           MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digestByte = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digestByte) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
