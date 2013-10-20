package com.granveaud.offheapbench.utils;

import java.nio.charset.Charset;

public class StringUtils {
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    // convert to/from ASCII strings
    // Warning: if the provided string contains non ASCII characters, they will be lost
    public static byte[] asciiStringToBytes(CharSequence str) {
        if (str == null) return null;

        byte[] res = new byte[str.length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) str.charAt(i);
        }

        return res;
    }

    public static String bytesToAsciiString(byte[] data) {
        if (data == null) return null;

        char[] chars = new char[data.length];
        for (int i = 0; i < data.length; i++) {
            chars[i] = (char) data[i];
        }

        return new StringBuilder(data.length).append(chars).toString();
    }

    public static byte[] stringToUTF8Bytes(String str) {
        if (str == null) return null;

        return str.getBytes(UTF8_CHARSET);
    }

    public static String utf8BytesToString(byte[] bytes) {
        if (bytes == null) return null;

        return new String(bytes, UTF8_CHARSET);
    }
}
