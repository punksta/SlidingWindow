package com.punksta.udp.old;

import com.punksta.udp.support.Cancable;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by com.punksta on 14.01.16.
 * http://mobiumapps.com/
 */
public class Reader implements Cancable {
    private final Object lock = new Object();
    private volatile int numberOfRequests = 0;
    private volatile boolean active = true;

    public Reader(Receiver receiver, FileInputStream file) {
        active = true;
        new Thread(() -> {
            synchronized (lock) {
                while (true) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                    while (numberOfRequests > 0 ) {
                        if (!active)
                            break;
                        try {
                            BytearrayWithIndex byteIntex = receiver.getReadingArray();
                            byte bytes[] = byteIntex.bytes;

                            int resultNumber = file.read(bytes);
                            resultNumber = resultNumber == -1 ? 0 : resultNumber;

                            if (resultNumber != bytes.length) {
                                receiver.readingFinished(byteIntex.index, resultNumber);
                                active = false;
                                Thread.interrupted();
                            } else {
                                receiver.onRead(byteIntex.index);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        numberOfRequests--;
                    }
                }
            }
        }).start();
    }


    @Override
    public void cancel() {
        synchronized (lock) {
            lock.notify();
            active = false;
        }
    }


    public interface Receiver {
        BytearrayWithIndex getReadingArray();
        void onRead(int index);
        void readingFinished(int index, int realSize);
    }

    public void readPart() {
        synchronized (lock) {
            numberOfRequests++;
            lock.notify();
        }
    }

}
