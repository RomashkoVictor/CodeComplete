package romashko.by.service;


import romashko.by.model.Header;
import romashko.by.model.Package;

import java.io.IOException;
import java.nio.channels.FileChannel;


public class PackageOutputBuffer implements AutoCloseable {
    private int numberOfBuffers;
    private int currentNumberOfBuffer;
    private FutureByteBuffer currentBuffer;
    private FutureByteBuffer[] buffers;

    public PackageOutputBuffer(FileChannel fileChannel, int numberOfBuffers) {
        if (numberOfBuffers < 1) {
            numberOfBuffers = 1;
        }
        this.numberOfBuffers = numberOfBuffers;
        buffers = new FutureByteBuffer[numberOfBuffers];
        for (int i = 0; i < numberOfBuffers; i++) {
            buffers[i] = new FutureByteBuffer(fileChannel);
        }
        currentNumberOfBuffer = 0;
        currentBuffer = buffers[currentNumberOfBuffer];
    }

    private FutureByteBuffer nextBuffer() {
        if (currentNumberOfBuffer + 1 == numberOfBuffers) {
            currentNumberOfBuffer = 0;
            return buffers[currentNumberOfBuffer];
        } else {
            return buffers[++currentNumberOfBuffer];
        }
    }

    private void exchangeBuffers() {
        DiskService.getDiskService().writeBuffer(currentBuffer);
        currentBuffer = nextBuffer();
        currentBuffer.waitIfNotReady();
    }

    public void writePackageHeader(Header header) {
        currentBuffer.waitIfNotReady();
        if (currentBuffer.remaining() < FutureByteBuffer.MAX_SIZE_OF_DATA) {
            exchangeBuffers();
        }
        currentBuffer.putLong(header.getLength());
        currentBuffer.putInt(header.getStartIndex());
        currentBuffer.putInt(header.getEndIndex());
    }

    public void writeDataOfPackage(Package pack) {
        currentBuffer.waitIfNotReady();
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

    public void writeDataOfPackage(Header header, PackageInputBuffer inputBuffer) {
        currentBuffer.waitIfNotReady();
        long length = header.getLength();
        int read = 0;
        while (length != 0) {
            if(read == -1){
                MainService.LOGGER.error("unexpected end of file");
            }
            int size = Math.min((int)Math.min((long)inputBuffer.remaining(), length), currentBuffer.remaining());
            byte[] temp = new byte[FutureByteBuffer.MAX_BUFFER_SIZE];
            read = inputBuffer.readData(temp, size);
            length -= size;
            currentBuffer.put(temp, 0, size);
            if (currentBuffer.remaining() == 0) {
                exchangeBuffers();
            }
        }
    }

    public void flush() {
        exchangeBuffers();
    }

    @Override
    public void close() throws IOException {
        flush();
        for (FutureByteBuffer buffer : buffers) {
            buffer.waitIfNotReady();
        }
        buffers[0].getFileChannel().close();
    }
}
