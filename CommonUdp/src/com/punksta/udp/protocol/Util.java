package com.punksta.udp.protocol;

import java.io.*;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class Util {

    public static byte[] toBytes(Serializable serializable) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutput out = new ObjectOutputStream(bos)) {
            out.writeObject(serializable);
            return bos.toByteArray();
        }
    }

    public static <T extends Serializable> T convertFromBytes(byte[] bytes) throws IOException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInput in = new ObjectInputStream(bis)) {
            return (T) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
