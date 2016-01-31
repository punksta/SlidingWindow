package com.punksta.udp.old;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by com.punksta on 14.01.16.
 * http://mobiumapps.com/
 */
public class ReadingSendingController implements
        Reader.Receiver,
        Sender.ResultReceiver {

    private Buffer buffer;
    private volatile int index;

    private Sender sender;
    private Reader reader;

    private long timeoutMilliSecond = 4000;
    private final Timer timer = new Timer();
    private volatile int last;

    public ReadingSendingController(int slidingSize, int filePartSize) {
        buffer = new Buffer(slidingSize, filePartSize);
    }


    @Override
    public BytearrayWithIndex getReadingArray() {
        try {
            BytearrayWithIndex result = new BytearrayWithIndex(index, buffer.getNewNumberArray(index));
            index ++;
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRead(int index) {
        sender.sendPart(buffer.getNumberArray(index), index);
    }

    @Override
    public void readingFinished(int index, int realSize) {
        last = index;
        sender.sendPart(buffer.getNumberArray(index), index);
    }


    @Override
    public void partSended(int number, long time) {
       buffer.setSend(number, time);
    }

    public void setReader(Reader reader) {
        this.reader = reader;
    }

    public void setSender(Sender sender) {
        this.sender = sender;
    }


    private void initTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                buffer.applyFunctionForTimeOutItems(System.currentTimeMillis(), timeoutMilliSecond, sender::sendPart);
            }
        }, timeoutMilliSecond/2, timeoutMilliSecond/2);
    }

}
