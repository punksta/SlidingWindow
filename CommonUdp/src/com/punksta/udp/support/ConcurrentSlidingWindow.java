package com.punksta.udp.support;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class ConcurrentSlidingWindow<T> {
    private final CyclingFixedSizeDeque<T> deque;
    protected final Object lock = new Object();
    private int currentStart;
    private int currentEnd;
    private int size;

    public ConcurrentSlidingWindow(int size) {
        deque = new CyclingFixedSizeDeque<>(size);
        this.size = size;
    }

    public T move() {
        synchronized (lock) {
            while (currentStart == currentEnd) {
                try {
                    lock.wait(300);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            currentStart ++;
            T result = deque.remove();
            lock.notify();
            return result;
        }
    }

    public void read(T data) {
        synchronized (lock) {
            while (currentEnd - currentStart >= size) {
                try {
                    lock.wait(300);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            currentEnd++;
            deque.push(data);
            lock.notify();
        }
    }

    public T get(int i) {
        synchronized (lock) {
            while (i - currentStart >= deque.realSize())
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            return deque.get(i - currentStart);
        }
    }


    public int getCurrentStart() {
        return currentStart;
    }

    public int getCurrentEnd() {
        return currentEnd;
    }


    public static void test(int slidingWindowSize) {

        ConcurrentSlidingWindow<Integer> slidingWindow = new ConcurrentSlidingWindow<>(slidingWindowSize);
        int size = slidingWindowSize * 10;

        final int[] sendPackages = new int[size];

        Runnable reader = () -> {
            for (int i = 0; i < size; i++) {
                slidingWindow.read(i);
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable sender = () -> {
            for (int i = 0; i < size; i++) {
                int packageToSend = slidingWindow.move();
                sendPackages[packageToSend]++;
                try {
                    Thread.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            boolean test = true;
            for (int countOfSendinds : sendPackages)
                if (countOfSendinds != 1) {
                    test = false;
                    break;
                }

            System.out.printf(test ? "test passed" : "test fail");
        };

        new Thread(reader).start();
        new Thread(sender).start();
    }

    public static void main(String[] args) {
        test(100);
    }

    public int getSize() {
        return size;
    }



    public T getFirstReading() {
        return deque.getHead();
    }

    public T getLastReading() {
        return deque.getTail();
    }
}
