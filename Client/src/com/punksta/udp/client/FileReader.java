package com.punksta.udp.client;

import com.punksta.udp.support.Cancable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
public class FileReader implements Cancable {
    private Thread thread;
    private volatile boolean active = true;

    public FileReader(File file, Supplier<byte[]> sourseArray, CallBack confirmation, Runnable onEnd) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(file);
        thread = new Thread(() -> {
            while (true) {
                if (!active)
                    break;
                try {
                    byte[] bytes = sourseArray.get();
                    int read = fileInputStream.read(bytes);
                    if (read == -1) {
                        onEnd.run();
                        break;
                    } else
                        confirmation.onRead(bytes, read);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void cancel() {
        active = false;
        thread.interrupt();
    }

    public interface CallBack {
        void onRead(byte[] data, int realSize);
    }
}
