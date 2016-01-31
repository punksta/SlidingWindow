package com.punksta.udp.support;

import java.util.LinkedList;

/**
 * Created by com.punksta on 29.01.16.
 * http://mobiumapps.com/
 */
public class Сhannel<T> {
    private final int nMax;
    private final LinkedList<T> list = new LinkedList<T>();
    private final Object lock = new Object();

    public Сhannel(int nMax) {
        this.nMax = nMax;
    }


    public T get() throws InterruptedException {
        synchronized (lock) {
            while (list.size() <= 0) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            lock.notify();
            return list.removeLast();
        }
    }

    public void put(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }

        synchronized (lock) {
            while (list.size() >= nMax) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            list.addFirst(obj);
            lock.notify();
        }
    }

    public int size() {
        synchronized (lock) {
            return list.size();
        }
    }
}
