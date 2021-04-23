package com.api.Autonova.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AesEncriptionUtil {


    private static final String globalKey = "Autopa20sskeygp7";

    public static String encrypt(String input) {
        byte[] crypted = null;
        try {

            SecretKeySpec skey = new SecretKeySpec(globalKey.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            crypted = cipher.doFinal(input.getBytes());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        Base64.Encoder encoder = Base64.getEncoder();

        return new String(encoder.encodeToString(crypted));
    }

    public static String decrypt(String input) {
        byte[] output = null;
        try {
            Base64.Decoder decoder = Base64.getDecoder();
            SecretKeySpec skey = new SecretKeySpec(globalKey.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skey);
            output = cipher.doFinal(decoder.decode(input));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        if(output != null){
            return new String(output);
        }else {
            return null;
        }
    }


}
