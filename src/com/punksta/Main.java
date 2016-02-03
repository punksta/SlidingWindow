package com.punksta;

import com.punksta.udp.Server.Server;
import com.punksta.udp.client.Client;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by punksta on 01.02.16.
 * http://mobiumapps.com/
 */
public class Main {
    private static final String inputFile = "1.txt";
    private static final int serverPort = 1231;
    private static final int packageSize = 264;
    private static final int slidingWindowSize = 5;

    public static void main(String[] args) throws IOException {

        new Server(packageSize, serverPort, slidingWindowSize);

        new Client(inputFile, slidingWindowSize, packageSize, InetAddress.getLocalHost(), serverPort);

    }

}
