package romashko.by.service;

import romashko.by.model.Package;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Queue;

public class FileService implements Runnable {

    private Thread thread;
    boolean running = false;
    private int frequency = 50;
    private Queue<PackageInputBuffer> inputBuffers;
    private Queue<ByteBuffer> outputBuffers;
    private Queue<FileChannel> outputFiles;
    private int numberOfBuffer;

    @Override
    public void run() {
        try {
            while (running) {
                if (!outputBuffers.isEmpty()) {
                    ByteBuffer buffer = outputBuffers.poll();
                    FileChannel in = outputFiles.poll();

                } else {
                    Thread.sleep(frequency);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void readBuffer(PackageInputBuffer input) {
        synchronized (input) {
            try {
                inputBuffers.add(input);
                input.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void writeBuffer(ByteBuffer buffer, FileChannel in) {
        outputBuffers.add(buffer);
        outputFiles.add(in);
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
            out.write(pack.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
