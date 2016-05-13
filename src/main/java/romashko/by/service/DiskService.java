package romashko.by.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DiskService {
    public static int readFromDisk(ByteBuffer inputBuffer, FileChannel in) {
        int readByte = -1;
        try {
            int i = 0;
            while (inputBuffer.remaining() != 0) {
                inputBuffer.put(i++, inputBuffer.get());
            }
            inputBuffer.clear();
            inputBuffer.position(i);
            readByte = in.read(inputBuffer);
            inputBuffer.flip();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return readByte;
    }

    public static void writeToDisk(ByteBuffer outputBuffer, FileChannel out) {
        try {
            outputBuffer.flip();
            out.write(outputBuffer);
            outputBuffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
