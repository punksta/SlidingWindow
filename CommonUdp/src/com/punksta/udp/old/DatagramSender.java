package com.punksta.udp.old;

import com.punksta.udp.support.Cancable;

/**
 * Created by com.punksta on 29.01.16.
 * http://mobiumapps.com/
 */
public class DatagramSender<T> implements Cancable {
//    private final Chanel<T> requests;
//    private volatile CallBack<T> callBack;
//    private Thread thread;
//
//    private volatile boolean active = true;
//
//    /**
//     *
//     * @param wrapper can map T -> datagram
//     * @param chanelSize maximum of requests in 1 moment
//     */
//    public DatagramSender(final int portNumber, DatagramWrapper<T> wrapper, int chanelSize) throws SocketException {
//        requests = new Chanel<>(chanelSize);
//        final DatagramSocket socket = new DatagramSocket();
//
//        thread = new Thread(() -> {
//            while (true) {
//                if (!active || socket.isClosed())
//                    break;
//                try {
//                    T post = requests.get();
//                    DatagramPacket packet = wrapper.wrap(post);
//                    packet.setPort(portNumber);
//                    packet.setAddress(InetAddress.getByName("localhost"));
//                    try {
//                        socket.send(packet);
//                        callBack.wasSend(post, System.currentTimeMillis());
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } catch (InterruptedException | UnknownHostException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
//        thread.start();
//    }

    public void requestSending(T data) {
//        if (data != null)
//            requests.put(data);
    }


    @Override
    public void cancel() {
//            active = false;
//            thread.interrupt();
    }
}

