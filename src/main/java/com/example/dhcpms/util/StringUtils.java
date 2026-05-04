// com.example.dhcpms.util.StringUtils.java
package com.example.dhcpms.util;

public class StringUtils {
    public static boolean hasText(String str) {
        return str != null && !str.isEmpty() && !str.isBlank();
    }
}