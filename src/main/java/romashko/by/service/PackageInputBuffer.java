package romashko.by.service;

import romashko.by.model.Package;
import static romashko.by.service.DiskService.readFromDisk;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PackageInputBuffer {
    private static final int BUFFER_SIZE = 8192;
    private ByteBuffer inputBuffer = (ByteBuffer) ByteBuffer.allocate(BUFFER_SIZE).position(BUFFER_SIZE);

    public Package readPackage(FileChannel in) {
        while (inputBuffer.remaining() < 8) {
            if (readFromDisk(inputBuffer, in) == -1) {
                return null;
            }
        }
        int length = inputBuffer.getInt() - 4;
        int num = inputBuffer.getInt();
        byte[] data = new byte[length];
        int position = 0;
        int size = Math.min(inputBuffer.remaining(), length);
        inputBuffer.get(data, position, size);
        length -= size;
        while (length != 0) {
            position += size;
            if (readFromDisk(inputBuffer, in) == -1) {
                return null;
            }
            size = Math.min(inputBuffer.remaining(), length);
            inputBuffer.get(data, position, size);
            length -= size;
        }
        return new Package(num, data);
    }
}
