package com.punksta.udp.old;

import com.punksta.udp.support.Сhannel;

/**
 * Created by com.punksta on 14.01.16.
 * http://mobiumapps.com/
 */
public class Sender<T> {
    private final Сhannel<T> requests;
    private final ResultReceiver<T> resultReceiver;

    public Sender(ResultReceiver resultReceiver, int size) {
        this.resultReceiver = resultReceiver;
        requests = new Сhannel<>(size);
    }

    public interface ResultReceiver<T> {

        void partSended(int index, long time);
    }

    public void sendPart(byte[] part, int index) {
        resultReceiver.partSended(index, System.currentTimeMillis()+index);
        System.out.println("sended " + index);
    }
}
