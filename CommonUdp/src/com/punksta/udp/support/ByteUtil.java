package com.punksta.udp.support;

import java.util.stream.IntStream;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class ByteUtil {
    public static int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
                ((bytes[1] & 0xFF) << 16) |
                ((bytes[2] & 0xFF) << 8) |
                (bytes[3] & 0xFF);
    }


    public static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }


    public static boolean test() {
        return IntStream.range(Integer.MIN_VALUE, Integer.MAX_VALUE)
                .parallel()
                .mapToObj(i -> {
                    byte[] bytes = intToByteArray(i);
                    return i == fromByteArray(bytes);
                })
                .filter(comparisonResult -> !comparisonResult)
                .findFirst()
                .isPresent();

    }
}


