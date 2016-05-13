package romashko.by.service;

import romashko.by.model.Package;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Queue;

public class FileService implements Runnable {
    private static final int BUFFER_SIZE = 8192;
    private ByteBuffer inputBuffer;
    private ByteBuffer outputBuffer;

    private Thread thread;
    boolean running = false;
    private int frequency;
    private Queue<Package[]> queueOfBuffers;
    private Queue<Integer> numberElementsInBuffer;
    private int numberOfBuffer;

    public FileService() {
        inputBuffer = (ByteBuffer) ByteBuffer.allocate(BUFFER_SIZE).position(BUFFER_SIZE);
        outputBuffer = (ByteBuffer) ByteBuffer.allocate(BUFFER_SIZE);
    }


    public int getNumberOfBuffer() {
        return numberOfBuffer;
    }

    @Override
    public void run() {
        try {
            while (running || !queueOfBuffers.isEmpty()) {
                if (!queueOfBuffers.isEmpty()) {
                    try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(numberOfBuffer + ".txt"))) {
                        numberOfBuffer++;
                        Package[] buffer = queueOfBuffers.poll();
                        int numberOfPackages = numberElementsInBuffer.poll();
                        Arrays.sort(buffer, 0, numberOfPackages);

                        for (int i = 0; i < numberOfPackages; i++) {
                            writePackage(buffer[i], out);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Thread.sleep(frequency);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void addBuffer(Package[] buffer, int numberOfElements) {
        queueOfBuffers.add(buffer);
        numberElementsInBuffer.add(numberOfElements);
    }

    public void startWriting() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stopWriting() {
        try {
            running = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void writePackage(Package pack, OutputStream out) {
        try {
            out.write(Package.getByteArrayFromInt(pack.getLength()));
            out.write(Package.getByteArrayFromInt(pack.getNum()));
            out.write(pack.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
