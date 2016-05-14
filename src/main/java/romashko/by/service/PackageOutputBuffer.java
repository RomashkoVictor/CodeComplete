package romashko.by.service;


import romashko.by.model.Package;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class PackageOutputBuffer implements AutoCloseable, Closeable {
    private ConcurrentByteBuffer currentBuffer;
    private ConcurrentByteBuffer reserveBuffer;

    public PackageOutputBuffer(FileChannel fileChannel) {
        currentBuffer = new ConcurrentByteBuffer(fileChannel);
        reserveBuffer = new ConcurrentByteBuffer(fileChannel);
    }

    private void exchangeBuffers() {
        try {
            synchronized (reserveBuffer) {
                while (reserveBuffer.isLocked()) {
                    reserveBuffer.wait();
                }
                ConcurrentByteBuffer temp = currentBuffer;
                currentBuffer = reserveBuffer;
                reserveBuffer = temp;
                reserveBuffer.setLocked(true);
            }
            DiskService.writeBuffer(reserveBuffer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writePackage(Package pack) {
        if (currentBuffer.remaining() < 8) {
            exchangeBuffers();
        }
        currentBuffer.putInt(pack.getLength());
        currentBuffer.putInt(pack.getNum());
        writeDataOfPackage(pack);
    }

    public void writeDataOfPackage(Package pack) {
        byte[] data = pack.getData();
        int length = data.length;
        int position = 0;
        int size = Math.min(currentBuffer.remaining(), length);
        currentBuffer.put(data, position, size);
        length -= size;
        while (length != 0) {
            position += size;
            exchangeBuffers();
            size = Math.min(currentBuffer.remaining(), length);
            currentBuffer.put(data, position, size);
            length -= size;
        }
    }

    public void flush() {
        exchangeBuffers();
        //currentBuffer.clear();
    }

    @Override
    public void close() throws IOException {
        fileChannel.close();
    }
}
