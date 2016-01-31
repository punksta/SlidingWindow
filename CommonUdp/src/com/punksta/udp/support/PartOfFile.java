package com.punksta.udp.support;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class PartOfFile {
    public final byte[] data;
    public final int number;

    public PartOfFile(byte[] data, int number) {
        this.data = data;
        this.number = number;
    }

    @Override
    public String toString() {
        return "PartOfFile{" +
                "number=" + number +
                '}';
    }
}
