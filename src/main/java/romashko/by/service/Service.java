package romashko.by.service;

import romashko.by.model.Package;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class Service {
    //private PackageOutputBuffer outputBuffer = new PackageOutputBuffer();
    private Package buffer[];
    private int currentNumberOfPackages;
    private int maxNumberOfPackages;
    private int maxSizeOfBuffer;
    private int currentSizeOfBuffer;
    private int numberOfBuffers;

    public Service(int maxNumberOfPackages, int maxSizeOfBuffer) {
        this.maxNumberOfPackages = maxNumberOfPackages;
        this.maxSizeOfBuffer = maxSizeOfBuffer;
        buffer = new Package[maxNumberOfPackages];
    }

//    public static Package readPackage(InputStream in) {
//        try {
//            byte[] buf = new byte[4];
//            if (in.read(buf) == 4) {
//                int length = Package.getIntFromByteArray(buf);
//                in.read(buf);
//                int num = Package.getIntFromByteArray(buf);
//                byte[] data = new byte[length - 4];
//                in.read(data);
//                return new Package(num, data);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    public static void writePackage(Package pack, OutputStream out) throws IOException {
//        out.write(Package.getByteArrayFromInt(pack.getLength()));
//        out.write(Package.getByteArrayFromInt(pack.getNum()));
//        out.write(pack.getData());
//    }

    public void writeBuffer(PackageOutputBuffer out) throws IOException {

        Arrays.sort(buffer, 0, currentNumberOfPackages);

        for (int i = 0; i < currentNumberOfPackages; i++) {
            out.writePackage(buffer[i]);
        }
        out.flush();
    }

    public void add(Package pack) {
        if (!canAddPackage(pack.getLength())) {
            try  {
                PackageOutputBuffer out = new PackageOutputBuffer(new FileOutputStream(numberOfBuffers + ".txt").getChannel());
                writeBuffer(out);

                buffer = new Package[maxNumberOfPackages];
                numberOfBuffers++;
                currentSizeOfBuffer = 0;
                currentNumberOfPackages = 0;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        buffer[currentNumberOfPackages] = pack;
        currentNumberOfPackages++;
        currentSizeOfBuffer += pack.getLength();
    }

    public void flushBuffer() {
        try {
            PackageOutputBuffer out = new PackageOutputBuffer(new FileOutputStream(numberOfBuffers + ".txt").getChannel());
            writeBuffer(out);

            buffer = new Package[maxNumberOfPackages];
            numberOfBuffers++;
            currentSizeOfBuffer = 0;
            currentNumberOfPackages = 0;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void createFile(String nameOfResultFile) {
        try {
            PackageOutputBuffer out = new PackageOutputBuffer(new FileOutputStream(nameOfResultFile).getChannel());

            PackageInputBuffer[] inputBuffers = new PackageInputBuffer[numberOfBuffers];
            File file[] = new File[numberOfBuffers];
            PriorityQueue<Package> queue = new PriorityQueue<>(numberOfBuffers);
            Integer[] values = new Integer[numberOfBuffers];
            for(int i = 0; i < numberOfBuffers; i++){
                values[i] = new Integer(i);
            }
            TreeMap<Package, Integer> map = new TreeMap<>();

            for (int i = 0; i < numberOfBuffers; i++) {
                file[i] = new File(i + ".txt");
                inputBuffers[i] = new PackageInputBuffer(new FileInputStream(file[i]).getChannel());
            }
            for (int i = 0; i < numberOfBuffers; i++) {
                Package pack = inputBuffers[i].readPackage();
                map.put(pack, values[i]);
                queue.add(pack);
            }
            while (queue.peek() != null) {
                Package temp = queue.poll();
                out.writeDataOfPackage(temp);
                int index = map.get(temp);
                map.remove(temp);
                temp = inputBuffers[index].readPackage();
                if(temp != null) {
                    map.put(temp, values[index]);
                    queue.add(temp);
                }
            }
            out.flush();
            for (int i = 0; i < numberOfBuffers; i++) {
                //inputBuffers[i].close();
                file[i].delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean canAddPackage(int addSize) {
        if (currentSizeOfBuffer + addSize < maxSizeOfBuffer && currentNumberOfPackages < maxNumberOfPackages) {
            return true;
        } else {
            return false;
        }
    }
}