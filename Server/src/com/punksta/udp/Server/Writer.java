package com.punksta.udp.Server;

import com.punksta.udp.support.Cancable;
import com.punksta.udp.protocol.InitPackage;
import com.punksta.udp.support.Сhannel;
import com.punksta.udp.support.PartOfFile;

import java.io.*;

/**
 * Created by com.punksta on 31.01.16.
 * http://mobiumapps.com/
 */
class Writer implements Cancable {
    private Thread thread;
    private final Сhannel<PartOfFile> chanel;
    private volatile boolean active = true;

    public Writer(int maxChannelSize) {
        chanel = new Сhannel<>(maxChannelSize);
    }
    public void init(InitPackage initPackage) {
        thread = new Thread(() -> {
            try {
                File file = new File(initPackage.fileName);
                OutputStream outputStream = new FileOutputStream("send" + file.getName());
                while (active) {
                    try {
                        PartOfFile partOfFile = chanel.get();
                        outputStream.write(partOfFile.data);
                        if (initPackage.totalPackageCount - 1 == partOfFile.number) {
                            cancel();
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        });
        thread.start();
    }

    @Override
    public void cancel() {
        active = false;
        thread.interrupt();
        System.out.println("Writer end work");
    }

    public void write(PartOfFile bytes) {
        chanel.put(bytes);
    }
}
