package com.punksta.udp.old;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main {
    private static final String INPUT_FILE = "in.jpg" ;
    private static final int slingSize = 10;


    public static class FileSender {

        public static void main(String[] args) {
            try {
                FileInputStream inputStream = new FileInputStream(INPUT_FILE);


                ReadingSendingController controller = new ReadingSendingController(slingSize, 10);

                Reader reader = new Reader(controller, inputStream);
//                Sender sender = new Sender(controller);

                controller.setReader(reader);
//                controller.setSender(sender);

                for (int i = 0; i < slingSize+10; i++) {
                    reader.readPart();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
    }
}
