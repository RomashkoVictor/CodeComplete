package romashko.by.service;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ConcurrentByteBuffer {
    public static final int BUFFER_SIZE = 8185;
    public static final int MAX_BUFFER_SIZE = 8192;
    private ByteBuffer buffer;
    private boolean locked;
    private FileChannel fileChannel;
    private boolean willClose;

    public ConcurrentByteBuffer(FileChannel fileChannel){
        this.fileChannel = fileChannel;
        willClose = false;
        buffer = ByteBuffer.allocate(MAX_BUFFER_SIZE);
        buffer.limit(BUFFER_SIZE);
        locked = false;
    }

    public FileChannel getFileChannel() {
        return fileChannel;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked= locked;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setWillClose(boolean willClose) {
        this.willClose = willClose;
    }

    public boolean isWillClose() {
        return willClose;
    }
    //Delegating FileChannel

    public int read(ByteBuffer dst) throws IOException {
        return fileChannel.read(dst);
    }

    public int write(ByteBuffer src) throws IOException {
        return fileChannel.write(src);
    }

    public void close() throws IOException {
        fileChannel.close();
    }

    public boolean isOpen() {
        return fileChannel.isOpen();
    }

    //Delegating ByteBuffer

    public int getInt() {
        return buffer.getInt();
    }

    public ByteBuffer put(byte[] src, int offset, int length) {
        return buffer.put(src, offset, length);
    }

    public ByteBuffer putInt(int value) {
        return buffer.putInt(value);
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

    public ByteBuffer put(int index, byte b) {
        return buffer.put(index, b);
    }

    public Buffer clear() {
        return buffer.clear().limit(ConcurrentByteBuffer.BUFFER_SIZE);
    }

    public int remaining() {
        return buffer.remaining();
    }

    public Buffer flip() {
        return buffer.flip();
    }
}
