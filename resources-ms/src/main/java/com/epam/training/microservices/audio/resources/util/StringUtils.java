package com.epam.training.microservices.audio.resources.util;

public class StringUtils {
    public static String removeNonASCII(String str) {
        if (str == null) {
            return "";
        }
        return str.replaceAll("[^\\x00-\\x7F]", "");
    }
}
