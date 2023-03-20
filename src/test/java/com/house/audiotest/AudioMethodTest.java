package com.house.audiotest;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AudioMethodTest {

    public static void main(String[] args) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");

        String inputStr = "";
        byte[] inputData = inputStr.getBytes();
        md.update(inputData);
        BigInteger bigInteger = new BigInteger(md.digest());

        System.out.println("MD5加密后:" + bigInteger.toString(16));
    }
}
