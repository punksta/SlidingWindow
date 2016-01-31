package com.punksta.udp.client;

import com.punksta.udp.protocol.InitPackage;
import com.punksta.udp.support.*;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class Client implements
        Cancable {
    private final Object monitor = new Object();

    private final ConcurrentSlidingWindow<TimedPartOfFile> slidingWindow;
    private final Сhannel<byte[]> readingBuffer;

    private final ClientSender clientSender;
    private final FileReader reader;
    private final Receiver receiver;

    private volatile int readingNumber = 0;

    private volatile int end = Integer.MAX_VALUE;

    private Timer timer = new Timer();

    private static final long timeOut = 1500;

    public Client(String fileName, int slidingWindowSize, int packageSize, InetAddress address, int serverPort) throws IOException {
        slidingWindow =  new ConcurrentSlidingWindow<>(slidingWindowSize);
        readingBuffer = new Сhannel<>(slidingWindowSize);

        for (int i = 0; i < slidingWindowSize; i++)
            readingBuffer.put(new byte[packageSize]);

        File file = new File(fileName);
        DatagramSocket socket = new DatagramSocket();


        byte[] initPackageByte = new InitPackage(file.length(), fileName, roundedNatural(file.length(), packageSize)).toBytes();
        TimedPartOfFile init = new TimedPartOfFile(initPackageByte, 0);

        slidingWindow.read(init);

        clientSender = new ClientSender(slidingWindowSize, packageSize, this::onPackageSend, socket, address, serverPort);

        clientSender.sent(init);
        reader = new FileReader(file, this::getReadingArray, this::onPartRead, this::onEnd);

        receiver = new Receiver(socket, this::onPackageConfirm);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
               resend();
            }
        }, timeOut, timeOut);
    }


    private void resend() {
        List<PartOfFile> resend = new ArrayList<>();
        synchronized (monitor) {
            for (int i = slidingWindow.getCurrentStart(); i < slidingWindow.getCurrentEnd(); i++) {
                TimedPartOfFile timedPartOfFile = slidingWindow.get(i);
                if (timedPartOfFile.timeOfSanding != 0 && !timedPartOfFile.confirm)
                    resend.add(timedPartOfFile);
            }
        }
        for (PartOfFile r:resend)
            if (r != null)
                clientSender.sent(r);
    }

    private void onEnd() {
        end = slidingWindow.getCurrentEnd();
        clientSender.sent(getDataGram());
    }

    private void onPartRead(byte[] bytes, int realSize) {
        if (bytes.length != realSize) {
            byte [] newB = new byte[realSize];
            System.arraycopy(bytes, 0, newB, 0, realSize);
            bytes = newB;
        }

        TimedPartOfFile partOfFile = new TimedPartOfFile(bytes, slidingWindow.getCurrentEnd());
        slidingWindow.read(partOfFile);
        clientSender.sent(getDataGram());

    }

    private long roundedNatural(long a, long b) {
        return (a+(b-1)) / b;
    }

    private byte[] getReadingArray() {
        try {
            return readingBuffer.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void onPackageConfirm(int pNubmer) {
        if (slidingWindow.getCurrentStart() > pNubmer)
            return;

        synchronized (monitor) {
            slidingWindow.get(pNubmer).confirm = true;

            while (slidingWindow.get(slidingWindow.getCurrentStart()).confirm) {
                byte[] freeBytes = slidingWindow.move().data;
                if (pNubmer != 0)
                    readingBuffer.put(freeBytes);
            }

            if (slidingWindow.getCurrentStart() == end)
                cancel();
        }
    }

    private PartOfFile getDataGram() {
       return slidingWindow.get(readingNumber++);
    }

    private void onPackageSend(Integer packageNumber) {
        long millisecond = System.currentTimeMillis();
        synchronized (monitor) {
            if (packageNumber >= slidingWindow.getCurrentStart()) {
                try {
                    slidingWindow.get(packageNumber).timeOfSanding = millisecond;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    System.err.println("onPackageSend " + packageNumber + " start:" + slidingWindow.getCurrentStart() + " end:" + slidingWindow.getCurrentEnd());
                }
            }
        }
    }

    @Override
    public void cancel() {
        clientSender.cancel();
        reader.cancel();
        receiver.cancel();
        timer.cancel();
    }



}
