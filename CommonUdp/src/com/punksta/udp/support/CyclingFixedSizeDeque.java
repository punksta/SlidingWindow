package com.punksta.udp.support;

import java.util.Arrays;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
class CyclingFixedSizeDeque<T> {
    private int head;
    private int tail;
    private final Object[] data;

    public CyclingFixedSizeDeque(int length) {
        this.data = new Object[length];
    }

    public void push(T elem) {
        data[(tail++) % data.length] = elem;
    }

    public T remove() {
        if (head == tail)
            throw new IllegalStateException("head = tail");
        int index = (head++) % data.length;
        final T result =  (T) data[index];
        data[index] = null;
        return result;
    }

    public T get(int i) {
        if (i >= data.length)
            throw new IllegalArgumentException("ahtung!");
        return (T) data[(head + i) % data.length];
    }

    public int realSize() {
        int realSize = 0;
        for (Object aData : data) {
            if (aData != null)
                realSize++;
        }
        return realSize;
//        return tail <= head ? (tail - head) : data.length - head - tail;
    }

    public int size() {
        return data.length;
    }

    @Override
    public String toString() {
        return "CyclingFixedSizeDeque{" +
                "head=" + head +
                ", tail=" + tail +
                ", data=" + Arrays.toString(data) +
                ", length=" + data.length +
                '}';
    }

    public T getHead() {
        return (T) data[head % size()];
    }

    public T getTail() {
        return (T) data[tail % size()];
    }
}
