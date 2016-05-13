package romashko.by.service;


import romashko.by.model.Package;
import static romashko.by.service.DiskService.writeToDisk;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class PackageOutputBuffer {
    private static final int BUFFER_SIZE = 8192;
    private ByteBuffer outputBuffer = (ByteBuffer) ByteBuffer.allocate(BUFFER_SIZE);

    public void writePackage(Package pack, FileChannel out) {
        if (outputBuffer.remaining() < 8) {
            writeToDisk(outputBuffer, out);
        }
        outputBuffer.putInt(pack.getLength());
        outputBuffer.putInt(pack.getNum());
        writeDataOfPackage(pack, out);
    }

    public void writeDataOfPackage(Package pack, FileChannel out) {
        byte[] data = pack.getData();
        int length = data.length;
        int position = 0;
        int size = Math.min(outputBuffer.remaining(), length);
        outputBuffer.put(data, position, size);
        length -= size;
        while (length != 0) {
            position += size;
            writeToDisk(outputBuffer, out);
            size = Math.min(outputBuffer.remaining(), length);
            outputBuffer.put(data, position, size);
            length -= size;
        }
    }

    public void flush(FileChannel out) {
        writeToDisk(outputBuffer, out);
        outputBuffer.clear();
    }
}
