package com.punksta.udp.support;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class TimedPartOfFile extends PartOfFile {
    public transient volatile long timeOfSanding;
    public transient volatile boolean confirm;

    public TimedPartOfFile(byte[] data, int number) {
        super(data, number);
    }
}
