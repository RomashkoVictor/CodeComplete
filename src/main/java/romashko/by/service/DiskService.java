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
    private static Queue<ConcurrentByteBuffer> inputBuffers = new ConcurrentLinkedQueue<>();
    private static Queue<ConcurrentByteBuffer> outputBuffers = new ConcurrentLinkedQueue<>();

    public static void readBuffer(ConcurrentByteBuffer buffer) {
        synchronized (DiskService.class) {
            inputBuffers.add(buffer);
            DiskService.class.notify();
        }
    }

    public static void writeBuffer(ConcurrentByteBuffer buffer) {
        synchronized (DiskService.class) {
            outputBuffers.add(buffer);
            DiskService.class.notify();
        }
    }

    public static void readFromDisk(ConcurrentByteBuffer inputBuffer){
        try {
            if (inputBuffer.isWillClose() || !inputBuffer.isOpen()) {
                inputBuffer.close();
                return;
            }
            int i = 0;
            int readByte = -1;
            while (inputBuffer.remaining() != 0) {
                inputBuffer.put(i++, inputBuffer.get());
            }
            inputBuffer.clear();
            inputBuffer.position(i);
            readByte = inputBuffer.read(inputBuffer.getBuffer());
            if (readByte == -1) {
                inputBuffer.close();
            }
            inputBuffer.flip();
            synchronized (inputBuffer) {
                inputBuffer.setLocked(false);
                inputBuffer.notify();
            }
        }catch(IOException e){
            Service.LOGGER.error(e);
        }
    }

    public static void writeToDisk(ConcurrentByteBuffer outputBuffer){
        try {
            if (outputBuffer.isWillClose()) {
                outputBuffer.close();
                return;
            }
            outputBuffer.flip();
            outputBuffer.write(outputBuffer.getBuffer());
            outputBuffer.clear();
            synchronized (outputBuffer) {
                outputBuffer.setLocked(false);
                outputBuffer.notify();
            }
        }catch(IOException e){
            Service.LOGGER.error(e);
        }
    }

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
                    synchronized (DiskService.class) {
                        if (outputBuffers.isEmpty() && inputBuffers.isEmpty()) {
                            DiskService.class.wait();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startService() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stopService() {
        try {
            running = false;
            synchronized (DiskService.class) {
                DiskService.class.notify();
            }
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
