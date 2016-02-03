package com.punksta.udp.client;

import com.punksta.udp.protocol.InitPackage;
import com.punksta.udp.support.*;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class Client implements
        Cancable {
    private final Object monitor = new Object();

    private final FileSlidingWidnow slidingWindow;
    private final Сhannel<byte[]> readingBuffer;

    private final ClientSender clientSender;
    private final FileReader reader;
    private final Receiver receiver;

    private volatile int readingNumber = 1;

    private volatile int end = Integer.MAX_VALUE;

    private Timer timer = new Timer();

    private static final long timeOut = 1500;

    public Client(String fileName, int slidingWindowSize, int packageSize, InetAddress address, int serverPort) throws IOException {
        slidingWindow =  new FileSlidingWidnow(slidingWindowSize);
        readingBuffer = new Сhannel<>(slidingWindowSize);

        for (int i = 0; i < slidingWindowSize; i++)
            readingBuffer.put(new byte[packageSize]);

        File file = new File(fileName);
        DatagramSocket socket = new DatagramSocket();


        long numberOfPackages = roundedNatural(file.length(), packageSize);
        long numberOfPackagesPlusInit = numberOfPackages + 1;

        byte[] initPackageByte = new InitPackage(file.length(), fileName, numberOfPackagesPlusInit).toBytes();
        TimedPartOfFile init = new TimedPartOfFile(initPackageByte, 0);

        slidingWindow.read(init);

        clientSender = new ClientSender(
                slidingWindowSize,
                packageSize,
                packageNumber -> slidingWindow.setSendingTime(packageNumber, System.currentTimeMillis()),
                socket,
                address,
                serverPort
        );

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
        slidingWindow.getNotConfirmedParts(timeOut).forEach(clientSender::sent);
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

        slidingWindow.setConfirm(pNubmer);
        for (TimedPartOfFile p: slidingWindow.moveWindow())
            if (p.number != 0)
                readingBuffer.put(p.data);
    }

    private PartOfFile getDataGram() {
       return slidingWindow.get(readingNumber++);
    }


    @Override
    public void cancel() {
        clientSender.cancel();
        reader.cancel();
        receiver.cancel();
        timer.cancel();
    }

    public static class FileSlidingWidnow extends  ConcurrentSlidingWindow<TimedPartOfFile> {

        public FileSlidingWidnow(int size) {
            super(size);
        }

        public Stream<TimedPartOfFile> getNotConfirmedParts(long timeOutInMilliseconds) {
            synchronized (lock) {
                long now = System.currentTimeMillis();
                return IntStream.range(getCurrentStart(), getCurrentEnd())
                        .mapToObj(this::get)
                        .filter(timedPartOfFile -> timedPartOfFile != null && !timedPartOfFile.confirm && timedPartOfFile.timeOfSanding != 0 && now - timedPartOfFile.timeOfSanding > timeOutInMilliseconds);
            }
        }

        public List<TimedPartOfFile> moveWindow() {
            synchronized (lock) {
                LinkedList<TimedPartOfFile> result = new LinkedList<>();
                while (get(getCurrentStart()).confirm) {
                    result.add(move());
                }
                return result;
            }
        }

        public void setSendingTime(int number, long sending) {
            synchronized (lock) {
                if (number >= getCurrentStart()) {
                    try {
                        get(number).timeOfSanding = sending;
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void setConfirm(int number) {
            synchronized (lock) {
                if (number >= getCurrentStart())
                    get(number).confirm = true;
            }
        }
    }
}
