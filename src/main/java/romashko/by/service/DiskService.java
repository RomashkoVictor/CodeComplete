package romashko.by.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DiskService implements Runnable {
    public static Thread thread;
    private boolean running = false;
    private static Queue<ConcurrentByteBuffer> inputBuffers = new LinkedList<>();
    private static Queue<ConcurrentByteBuffer> outputBuffers = new LinkedList<>();

    @Override
    public void run() {
        try {
            while (running || !inputBuffers.isEmpty() || !outputBuffers.isEmpty()) {
                if (!inputBuffers.isEmpty()) {
                    ConcurrentByteBuffer buffer = inputBuffers.poll();
                    DiskService.readFromDisk(buffer);
                } else if (!outputBuffers.isEmpty()) {
                    ConcurrentByteBuffer buffer = outputBuffers.poll();
                    DiskService.writeToDisk(buffer);
                } else {
                    Thread.yield();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void readBuffer(ConcurrentByteBuffer buffer) {
        inputBuffers.add(buffer);
    }

    public static void writeBuffer(ConcurrentByteBuffer buffer) {
        outputBuffers.add(buffer);
    }

    public void startService() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stopService() {
        try {
            running = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void readFromDisk(ConcurrentByteBuffer inputBuffer) throws IOException {
        int i = 0;
        while (inputBuffer.remaining() != 0) {
            inputBuffer.put(i++, inputBuffer.get());
        }
        inputBuffer.clear();
        inputBuffer.position(i);
        if (inputBuffer.read(inputBuffer.getBuffer()) == -1) {
            inputBuffer.close();
        }
        inputBuffer.flip();
        synchronized (inputBuffer) {
            inputBuffer.setLocked(false);
            inputBuffer.notify();
        }
    }

    public static void writeToDisk(ConcurrentByteBuffer outputBuffer) throws IOException {
        outputBuffer.flip();
        outputBuffer.write(outputBuffer.getBuffer());
        outputBuffer.clear();
        synchronized (outputBuffer) {
            outputBuffer.setLocked(false);
            outputBuffer.notify();
        }
    }
}
