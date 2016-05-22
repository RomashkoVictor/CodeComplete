package romashko.by.service;

import romashko.by.model.Header;
import romashko.by.model.Package;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PackageInputBuffer implements AutoCloseable {
    private int numberOfBuffers;
    private int currentNumberOfBuffer;
    private FutureByteBuffer currentBuffer;
    private FutureByteBuffer[] buffers;

    public PackageInputBuffer(FileChannel fileChannel, int numberOfBuffers) {
        if (numberOfBuffers < 1) {
            numberOfBuffers = 1;
        }
        this.numberOfBuffers = numberOfBuffers;
        buffers = new FutureByteBuffer[numberOfBuffers];
        for (int i = 0; i < numberOfBuffers; i++) {
            buffers[i] = new FutureByteBuffer(fileChannel);
            buffers[i].position(FutureByteBuffer.BUFFER_SIZE);
            DiskService.getDiskService().readBuffer(buffers[i]);
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

    private boolean isEndOfFile(){
        currentBuffer.waitIfNotReady();
        if(currentBuffer.remaining() > 0){
            return false;
        }
        return true;
    }

    private boolean sliceRemainingData(){
        if (numberOfBuffers > 1 && currentBuffer.remaining() != 0) {
            int oldNumberOfBuffer = currentNumberOfBuffer;
            FutureByteBuffer nextBuffer = nextBuffer();

            nextBuffer.waitIfNotReady();

            int addSize = FutureByteBuffer.MAX_SIZE_OF_DATA - currentBuffer.remaining();
            currentBuffer.position(currentBuffer.limit());
            currentBuffer.limit(currentBuffer.limit() + addSize);
            while (currentBuffer.remaining() != 0 && nextBuffer.remaining() != 0) {
                currentBuffer.put(nextBuffer.get());
            }
            currentBuffer.position(currentBuffer.position() - FutureByteBuffer.MAX_SIZE_OF_DATA);
            currentNumberOfBuffer = oldNumberOfBuffer;
            return true;
        }
        return false;
    }

    private void exchangeBuffers() {
        if(sliceRemainingData()){
            return;
        }
        DiskService.getDiskService().readBuffer(currentBuffer);
        currentBuffer = nextBuffer();
        currentBuffer.waitIfNotReady();
    }

    public Header readPackageHeader() {
        currentBuffer.waitIfNotReady();
        while (currentBuffer.remaining() < FutureByteBuffer.MAX_SIZE_OF_DATA) {
            exchangeBuffers();
            if(isEndOfFile()){
                return null;
            }
        }
        long length = currentBuffer.getLong();
        int startIndex = currentBuffer.getInt();
        int endIndex = currentBuffer.getInt();
        return new Header(length, startIndex, endIndex);
    }

    public Package readDataOfPackage(Header header) {
        if(header == null){
            return null;
        }
        currentBuffer.waitIfNotReady();
        long length = header.getLength();
        byte[] data = new byte[(int)length];
        int position = 0;
        int size = (int)Math.min(currentBuffer.remaining(), length);
        currentBuffer.get(data, position, size);
        length -= size;

        while (length != 0) {
            position += size;
            exchangeBuffers();
            if(isEndOfFile()){
                return null;
            }
            size = (int)Math.min(currentBuffer.remaining(), length);
            currentBuffer.get(data, position, size);
            length -= size;
        }
        return new Package(header, data);
    }

    public int readData(byte[] dst, int length) {
        currentBuffer.waitIfNotReady();
        currentBuffer.get(dst, 0, length);
        if(remaining() == 0){
            exchangeBuffers();
            if(isEndOfFile()){
                return -1;
            }
        }
        return length;
    }

    public int remaining(){
        return currentBuffer.remaining();
    }

    @Override
    public void close() throws IOException {
        for (FutureByteBuffer buffer : buffers) {
            buffer.waitIfNotReady();
        }
        buffers[0].getFileChannel().close();
    }
}
