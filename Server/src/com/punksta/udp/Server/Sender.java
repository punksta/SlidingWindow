package com.punksta.udp.Server;

import com.punksta.udp.support.ByteUtil;
import com.punksta.udp.support.Cancable;
import com.punksta.udp.support.Сhannel;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
class Sender implements Cancable {
    private Thread thread;
    private volatile boolean active = true;
    private final Сhannel<Integer> packetChannel;
    private final DatagramSocket socket;

    public Sender(int chanalSize,
                  DatagramSocket socket
                 ) throws SocketException {
        packetChannel = new Сhannel<>(chanalSize);
        this.socket = socket;
    }

    void init( InetAddress address,
               int port) {
        if (thread == null) {
            thread = new Thread(() -> {
                while (true) {
                    if (!active || socket.isClosed())
                        break;
                    try {
                        DatagramPacket packet = null;
                        try {
                            Integer partOfFile = packetChannel.get();
                            packet = new DatagramPacket(ByteUtil.intToByteArray(partOfFile), 4, address, port);
                            packet.setPort(port);
                            packet.setAddress(address);
                            socket.send(packet);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
    }

    @Override
    public void cancel() {
        active = false;
        if (thread != null)
            thread.interrupt();
        System.out.println("Sender end work");
    }

    public void sent(Integer packet) {
        packetChannel.put(packet);
    }
}

