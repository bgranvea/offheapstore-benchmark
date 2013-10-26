package com.granveaud.offheapbench.utils;

import java.nio.charset.Charset;

public class StringUtils {
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public static byte[] stringToUTF8Bytes(String str) {
        if (str == null) return null;

        return str.getBytes(UTF8_CHARSET);
    }

    public static String utf8BytesToString(byte[] bytes) {
        if (bytes == null) return null;

        return new String(bytes, UTF8_CHARSET);
    }
}
