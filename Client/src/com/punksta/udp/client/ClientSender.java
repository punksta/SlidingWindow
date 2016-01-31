package com.punksta.udp.client;

import com.punksta.udp.support.Cancable;
import com.punksta.udp.support.ByteUtil;
import com.punksta.udp.support.Сhannel;
import com.punksta.udp.support.PartOfFile;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.function.Consumer;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
class ClientSender implements Cancable {
    private Thread thread;
    private volatile boolean active = true;
    private final Сhannel<PartOfFile> packetChannel;
    private final byte[] buffer;

    public ClientSender(int chanalSize,
                        int packageSize,
                        Consumer<Integer> sendingConfirmation,
                        DatagramSocket socket,
                        InetAddress address,
                        int port) throws SocketException {
        packetChannel = new Сhannel<>(chanalSize);

        thread = new Thread(() -> {
            while (true) {
                if (!active || socket.isClosed())
                    break;
                try {
                    DatagramPacket packet = null;
                    try {
                        PartOfFile partOfFile = packetChannel.get();
                        packet = wrapPartOfFile(partOfFile);
                        packet.setPort(port);
                        packet.setAddress(address);
                        socket.send(packet);
                        sendingConfirmation.accept(partOfFile.number);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        buffer = new byte[packageSize + 4];
    }

    @Override
    public void cancel() {
        active = false;
        thread.interrupt();
        System.out.println("ClientSender end work");
}

    public void sent(PartOfFile packet) {
        packetChannel.put(packet);
    }


    public DatagramPacket wrapPartOfFile(PartOfFile partOfFile) {
//        assert buffer.length == partOfFile.data.length + 4;

        byte allBytes[] = buffer;
        byte number[] = ByteUtil.intToByteArray(partOfFile.number);

        //copy number to 0 - 4 bytes
        System.arraycopy(number, 0, allBytes, 0, 4);

        //copy data to 4 - last bytes
        System.arraycopy(partOfFile.data, 0, allBytes, 4, partOfFile.data.length);

        return new DatagramPacket(allBytes,  partOfFile.data.length + 4);
    }
}
