package com.lz.devflow.util;

import java.util.Base64;

public class Base64Utils {

    public static String encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public static String encode(byte[] input) {
        return Base64.getEncoder().encodeToString(input);
    }
}
