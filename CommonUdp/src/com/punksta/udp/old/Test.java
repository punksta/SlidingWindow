package com.punksta.udp.old;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Created by com.punksta on 28.01.16.
 * http://mobiumapps.com/
 */
public class Test {
    private static long activeThreadInParallel(int range) {
        return IntStream.range(0, range).parallel().mapToLong(i -> Thread.currentThread().getId()).distinct().count();
    }

    private static boolean bufferTest(int bufferSize, int arrayLenth) {
        final Buffer buffer = new Buffer(bufferSize, 300);
        final Random random = new Random();
        return
                IntStream.range(0, arrayLenth)
                        .parallel()
                        .mapToObj(partOfFile -> {
                            try {
                                buffer.getNewNumberArray(partOfFile);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                return false;
                            }
                            buffer.getNumberArray(partOfFile);
                                try {
                                    Thread.sleep(Math.abs(random.nextInt()) % 100);
                                } catch(InterruptedException ex) {
                                    ex.printStackTrace();
                                    Thread.currentThread().interrupt();
                                }
                                buffer.makeNumberFree(partOfFile);
                                try {
                                    buffer.getNumberArray(partOfFile);
                                    return false;
                                } catch (Exception e) {
                                    return true;
                                }
                        })
                        .allMatch(test -> test);
    }


    private static boolean readerTest(FileInputStream fileInputStream) {
        return true;
    }

    public static void main(String[] args) throws FileNotFoundException {
        System.out.println("active_threads " + activeThreadInParallel(300));
        System.out.println("buffer_test_parallel " +  bufferTest(3, 25));
        System.out.println("reader_test_parallel " + readerTest(new FileInputStream("in.jpg")));

    }

}
