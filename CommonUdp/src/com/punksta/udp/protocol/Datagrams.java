package com.punksta.udp.protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class Datagrams {
    public static DatagramPacket buildFirstDatagram(InitPackage initPackage, InetAddress address, int port) throws IOException {
        byte bytes[] = Util.toBytes(initPackage);
        return new DatagramPacket(bytes, 0, bytes.length, address, port);
    }
}
