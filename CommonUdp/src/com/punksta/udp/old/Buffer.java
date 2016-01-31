package com.punksta.udp.old;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

/**
 * Created by com.punksta on 28.01.16.
 * http://mobiumapps.com/
 */
public class Buffer {
    private volatile byte[][] bytes; //
    private volatile int[] indexes; // index -> number of bytes[index]
    private volatile Long[] timeOfSending; // send -> time of sending bytes[send]

    private final int slidingWindowSize;
    private final int partFileSize;

    private final Object lock = new Object();

    private static final int INDEX_NONE = -1;
    private static final Long TIME_NONE = -1L;

    public Buffer(int slidingWindowSize, int partFileSize) {
        this.slidingWindowSize = slidingWindowSize;
        this.partFileSize = partFileSize;
        initArrays();
    }

    public void makeNumberFree(final int number) {
        if (number >= 0)
            synchronized (lock) {
                getIndexOfNumber(number).ifPresent(index -> {
                    indexes[index] = INDEX_NONE;
                    lock.notify();
                });
            }
    }

    public byte[] getNewNumberArray(final int number) throws InterruptedException {
        synchronized (lock) {
            OptionalInt optionalIndex;
            while (!(optionalIndex = getFreeIndex()).isPresent())
                lock.wait();

            int freeIndex = optionalIndex.getAsInt();
            indexes[freeIndex] = number;
            timeOfSending[freeIndex] = TIME_NONE;
            return bytes[freeIndex];
        }
    }
    public byte[] getNumberArray(final int number) {
        return bytes[getIndexOfNumber(number).getAsInt()];
    }


    private OptionalInt getIndexOfNumber(int number) {
        return IntStream.range(0, indexes.length).filter(i -> indexes[i] == number).findFirst();
    }

    private OptionalInt getFreeIndex() {
        return IntStream.range(0, indexes.length).filter(i-> indexes[i] == INDEX_NONE).findAny();
    }

    private void initArrays() {
        bytes = new byte[slidingWindowSize][partFileSize];
        indexes = new int[slidingWindowSize];
        timeOfSending = new Long[slidingWindowSize];

        Arrays.fill(indexes, INDEX_NONE);
        Arrays.fill(timeOfSending, TIME_NONE);

        for (int i = 0; i < slidingWindowSize; i++) {
            bytes[i] = new byte[partFileSize];
        }
    }

    public void setSend(int number, Long time) {
        getIndexOfNumber(number).ifPresent(index -> timeOfSending[index] = time);
    }

    public Long getSend(int number) {
        return timeOfSending[getIndexOfNumber(number).getAsInt()];
    };

    public void applyFunctionForTimeOutItems(long current, long timeout, BiConsumer<byte[], Integer> resend) {
        for (int i = 0; i < timeOfSending.length; i++) {
            long iTime = timeOfSending[i];
            if (iTime != TIME_NONE && current - iTime > timeout)
                resend.accept( bytes[i], indexes[i]);
        }
    }
}
