package org.theanarch.onionrouting.Network;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.KeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Crypto {

    //public static void test(){
        //StreamBlockCipher cipher = new CFBBlockCipher(new AESFastEngine(), 16);
    //}

    public static KeyPair generateKeyPair()throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();

        return pair;
    }

    public static String rsaEncrypt(byte[] plainText, PublicKey publicKey)throws Exception {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

        byte[] cipherText = encryptCipher.doFinal(plainText);

        return Base64.getEncoder().encodeToString(cipherText);
    }

    public static String rsaDecrypt(String cipherText, PrivateKey privateKey)throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);

        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);

        return new String(decryptCipher.doFinal(bytes));
    }

    public static String sign(String plainText, PrivateKey privateKey)throws Exception {
        Signature privateSignature = Signature.getInstance("SHA512withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();

        return Base64.getEncoder().encodeToString(signature);
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey)throws Exception {
        Signature publicSignature = Signature.getInstance("SHA512withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes(UTF_8));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }






    public static String aesEncrypt(String plainText, String password)throws Exception {
        byte[] salt = new byte[8], iv = new byte[16];

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secret, spec);

        return Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes()));

        //CBC METHOD
        /*
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));

        return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));
        */
    }

    public static String aesDecrypt(String cipherText, String password)throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);
        byte[] salt = new byte[8], iv = new byte[16];

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec params = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secret, params);

        return new String(cipher.doFinal(bytes));

        //CBC METHOD
        /*
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

        return new String(cipher.doFinal(m));
        */
    }



    public static byte[] aesEncryptBytes(byte[] plainText, String password)throws Exception {
        byte[] salt = new byte[8], iv = new byte[16];

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, secret, spec);

        return Base64.getEncoder().encode(cipher.doFinal(plainText));

        //CBC METHOD
        /*
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(iv));

        return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));
        */
    }

    public static byte[] aesDecryptBytes(byte[] cipherText, String password)throws Exception {
        byte[] bytes = Base64.getDecoder().decode(cipherText);
        byte[] salt = new byte[8], iv = new byte[16];

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec params = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secret, params);

        return cipher.doFinal(bytes);

        //CBC METHOD
        /*
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));

        return new String(cipher.doFinal(m));
        */
    }





    public static String hashString(String request)throws Exception {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        byte[] buffer = messageDigest.digest(request.getBytes());

        StringBuffer sb = new StringBuffer(buffer.length*2);
        for(int i = 0; i < buffer.length; i++){
            int v = buffer[i] & 0xff;
            if(v < 16){
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }

        return sb.toString().toUpperCase();
    }

    public static String hashStringWSalt(String request)throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[128];
        random.nextBytes(salt);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        messageDigest.update(salt);
        byte[] buffer = messageDigest.digest(request.getBytes());

        StringBuffer sb = new StringBuffer(buffer.length*2);
        for(int i = 0; i < buffer.length; i++){
            int v = buffer[i] & 0xff;
            if(v < 16){
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }

        return sb.toString().toUpperCase();
    }
}
