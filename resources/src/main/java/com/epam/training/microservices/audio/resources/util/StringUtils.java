package com.epam.training.microservices.audio.resources.util;

import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

public class StringUtils {
    public static String encode(String str) {
        return UriUtils.encode(str, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
    }
}
