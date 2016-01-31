package com.punksta.udp.support;

import java.net.DatagramPacket;
import java.util.function.Function;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class DatagramTranslator {

    public static Function<Integer, DatagramPacket> confirmationToPackage = packageNumber -> new DatagramPacket(ByteUtil.intToByteArray(packageNumber), 4);
    public static Function<DatagramPacket, Integer> packageToConfirmation = datagram -> ByteUtil.fromByteArray(datagram.getData());


    public static PartOfFile unWrap(DatagramPacket packet) {
        //get number from 0 - 4 bytes
        byte numberInBytes[] = new byte[4];
        System.arraycopy(packet.getData(), 0, numberInBytes, 0, 4);
        int number = ByteUtil.fromByteArray(numberInBytes);

        int dataSize = packet.getLength() - 4;
        byte data[] = new byte[dataSize];
        System.arraycopy(packet.getData(), 4, data, 0, dataSize);
        return new PartOfFile(data, number);

    }
}
