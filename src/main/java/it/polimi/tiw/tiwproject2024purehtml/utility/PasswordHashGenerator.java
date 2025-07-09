package it.polimi.tiw.tiwproject2024purehtml.utility;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHashGenerator {
    public static void main(String [] args) throws NoSuchAlgorithmException {
        String password = "password5";
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
        byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
        System.out.println(HexString.toHexString(hash));
    }
}
