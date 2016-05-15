package romashko.by.service;

import romashko.by.model.Package;

import java.nio.channels.FileChannel;

public class PackageInputBuffer implements AutoCloseable, Cloneable {
    private ConcurrentByteBuffer currentBuffer;
    private ConcurrentByteBuffer reserveBuffer;

    public PackageInputBuffer(FileChannel fileChannel) {
        currentBuffer = new ConcurrentByteBuffer(fileChannel);
        reserveBuffer = new ConcurrentByteBuffer(fileChannel);
        currentBuffer.position(ConcurrentByteBuffer.BUFFER_SIZE);
        reserveBuffer.position(ConcurrentByteBuffer.BUFFER_SIZE);
    }

    private void exchangeBuffers() {
        try {
            synchronized (reserveBuffer) {
                while (reserveBuffer.isLocked()) {
                    reserveBuffer.wait();
                }
                if (currentBuffer.remaining() != 0) {
                    int addSize = 8 - currentBuffer.remaining();
                    currentBuffer.position(currentBuffer.limit());
                    currentBuffer.limit(currentBuffer.limit() + addSize);
                    while (currentBuffer.remaining() != 0) {
                        currentBuffer.put(reserveBuffer.get());
                    }
                    currentBuffer.position(currentBuffer.position() - 8);
                    return;
                }
                ConcurrentByteBuffer temp = currentBuffer;
                currentBuffer = reserveBuffer;
                reserveBuffer = temp;
                reserveBuffer.setLocked(true);
            }
            DiskService.readBuffer(reserveBuffer);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Package readPackage() {
        while (currentBuffer.remaining() < 8) {
            if (currentBuffer.isOpen()) {
                exchangeBuffers();
            } else {
                return null;
            }
        }
        int length = currentBuffer.getInt() - 4;
        int num = currentBuffer.getInt();
        byte[] data = new byte[length];
        int position = 0;
        int size = Math.min(currentBuffer.remaining(), length);
        currentBuffer.get(data, position, size);
        length -= size;

        while (length != 0) {
            position += size;
            if (currentBuffer.isOpen()) {
                exchangeBuffers();
            } else {
                return null;
            }
            size = Math.min(currentBuffer.remaining(), length);
            currentBuffer.get(data, position, size);
            length -= size;
        }
        return new Package(num, data);
    }

    @Override
    public void close() throws Exception {
        currentBuffer.position(currentBuffer.limit());
        currentBuffer.setWillClose(true);
        DiskService.readBuffer(currentBuffer);
    }
}
