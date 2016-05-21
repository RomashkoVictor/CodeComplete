package romashko.by.service;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static romashko.by.service.MainService.*;

public class DiskService implements Runnable {
    private static final DiskService DISK_SERVICE = new DiskService();
    public Thread thread;
    private boolean running = false;
    private Queue<FutureByteBuffer> inputBuffers = new ConcurrentLinkedQueue<>();
    private Queue<FutureByteBuffer> outputBuffers = new ConcurrentLinkedQueue<>();

    private DiskService(){
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public static DiskService getDiskService(){
        return DISK_SERVICE;
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

    public void readBuffer(FutureByteBuffer buffer) {
        synchronized (DiskService.class) {
            buffer.lock();
            inputBuffers.add(buffer);
            DiskService.class.notify();
        }
    }

    public void writeBuffer(FutureByteBuffer buffer) {
        synchronized (DiskService.class) {
            buffer.lock();
            outputBuffers.add(buffer);
            DiskService.class.notify();
        }
    }

    public static void readFromDisk(FutureByteBuffer inputBuffer){
        try {
            int addSize = 0;
            while (inputBuffer.remaining() != 0) {
                if(inputBuffer.remaining()>8){
                    System.out.println("!" + inputBuffer.remaining());
                }
                inputBuffer.put(addSize++, inputBuffer.get());
            }
            inputBuffer.clear();
            inputBuffer.position(addSize);
            inputBuffer.read(inputBuffer.getBuffer());
            inputBuffer.flip();
            synchronized (inputBuffer) {
                inputBuffer.unlock();
                inputBuffer.notify();
            }
        }catch(IOException e){
            LOGGER.error(e);
            e.printStackTrace();
        }
    }

    public static void writeToDisk(FutureByteBuffer outputBuffer){
        try {
            outputBuffer.flip();
            outputBuffer.write(outputBuffer.getBuffer());
            outputBuffer.clear();
            synchronized (outputBuffer) {
                outputBuffer.unlock();
                outputBuffer.notify();
            }
        }catch(IOException e){
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    @Override
    public void run() {
        try {
            while (running || !inputBuffers.isEmpty() || !outputBuffers.isEmpty()) {
                if (!inputBuffers.isEmpty()) {
                    FutureByteBuffer buffer = inputBuffers.poll();
                    DiskService.readFromDisk(buffer);
                } else if (!outputBuffers.isEmpty()) {
                    FutureByteBuffer buffer = outputBuffers.poll();
                    DiskService.writeToDisk(buffer);
                } else {
                    synchronized (DiskService.class) {
                        if (outputBuffers.isEmpty() && inputBuffers.isEmpty()) {
                            DiskService.class.wait();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
    }
}
