package com.punksta.udp.client;

import com.punksta.udp.support.Cancable;
import com.punksta.udp.support.DatagramTranslator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.function.Consumer;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class Receiver implements Cancable {
    private final Thread thread;
    private volatile boolean active = true;

    public Receiver(DatagramSocket socket, Consumer<Integer> confirm) {
        thread = new Thread(() -> {
            while (active) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[4], 4);
                    socket.receive(packet);
                    Integer integer = DatagramTranslator.packageToConfirmation.apply(packet);

                    confirm.accept(integer);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void cancel() {
        active = false;
        thread.interrupt();
    }
}
