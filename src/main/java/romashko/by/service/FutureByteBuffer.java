package romashko.by.service;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static romashko.by.service.MainService.*;

public class FutureByteBuffer {
    public static final int MAX_SIZE_OF_DATA = 16;
    public static final int MAX_BUFFER_SIZE = 8192;
    public static final int BUFFER_SIZE = MAX_BUFFER_SIZE - MAX_SIZE_OF_DATA;
    private final FileChannel fileChannel;
    private ByteBuffer buffer;
    private volatile boolean ready;

    public FutureByteBuffer(FileChannel fileChannel) {
        this.fileChannel = fileChannel;
        buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
        buffer.limit(BUFFER_SIZE);
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void waitIfNotReady() {
        try {
            synchronized (this) {
                while (!ready) {
                    this.wait();
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error(e);
        }
    }



    public void lock() {
        ready = false;
    }

    public void unlock() {
        ready = true;
    }

    //Delegating FileChannel

    public int read(ByteBuffer dst) throws IOException {
        return fileChannel.read(dst);
    }

    public int write(ByteBuffer src) throws IOException {
        return fileChannel.write(src);
    }

    //Delegating ByteBuffer

    public int getInt() {
        return buffer.getInt();
    }

    public long getLong() {
        return buffer.getLong();
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        return buffer.put(src, offset, length);
    }

    public ByteBuffer putInt(int value) {
        return buffer.putInt(value);
    }

    public ByteBuffer putLong(long value) {
        return buffer.putLong(value);
    }

    public ByteBuffer put(byte b) {
        return buffer.put(b);
    }

    public byte get() {
        return buffer.get();
    }

    public ByteBuffer get(byte[] dst, int offset, int length) {
        return buffer.get(dst, offset, length);
    }



    public ByteBuffer put(int index, byte b) {
        return buffer.put(index, b);
    }

    public Buffer position(int newPosition) {
        return buffer.position(newPosition);
    }

    public int limit() {
        return buffer.limit();
    }

    public Buffer limit(int newLimit) {
        return buffer.limit(newLimit);
    }

    public int position() {
        return buffer.position();
    }

    public Buffer clear() {
        return buffer.clear().limit(FutureByteBuffer.BUFFER_SIZE);
    }

    public int remaining() {
        return buffer.remaining();
    }

    public Buffer flip() {
        return buffer.flip();
    }
}
