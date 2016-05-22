package romashko.by.service;

import romashko.by.model.FileMerge;
import romashko.by.model.Header;
import romashko.by.model.Package;

import java.io.*;
import java.util.*;

import static romashko.by.service.MainService.*;

public class FileService {
    private Package buffer[];
    private int currentNumberOfPackages;
    private int maxNumberOfPackages;
    private int maxSizeOfBuffer;
    private int currentSizeOfBuffer;
    private static int numberOfBuffer;

    public FileService(int maxNumberOfPackages, int maxSizeOfBuffer) {
        this.maxNumberOfPackages = maxNumberOfPackages;
        this.maxSizeOfBuffer = maxSizeOfBuffer;
        buffer = new Package[maxNumberOfPackages];
    }

    public static String getName() {
        String res = dir + numberOfBuffer + ".txt";
        numberOfBuffer++;
        return res;
    }

    public void writeBuffer() {
        FileMerge file = new FileMerge(new File(getName()));
        try (PackageOutputBuffer out = new PackageOutputBuffer(new FileOutputStream(file.getFile()).getChannel(), 10);
             PackageOutputBuffer outHeader = new PackageOutputBuffer(new FileOutputStream(file.getFileHeader()).getChannel(), 5)) {
            Arrays.sort(buffer, 0, currentNumberOfPackages);

            Header prev = null;
            for (int i = 0; i < currentNumberOfPackages; i++) {
                out.writeDataOfPackage(buffer[i]);
                prev = MergeService.addToCluster(prev, buffer[i].getHeader(), outHeader);
            }
            if (prev != null) {
                outHeader.writePackageHeader(prev);
            }

            buffer = new Package[maxNumberOfPackages];
            currentSizeOfBuffer = 0;
            currentNumberOfPackages = 0;
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
        MergeService.addMergeFile(file);
    }


    public void add(Package pack) {
        if (!canAddPackage(pack.getLength())) {
            writeBuffer();
        }
        buffer[currentNumberOfPackages++] = pack;
        currentSizeOfBuffer += pack.getLength();
    }

    public void closeBuffer() {
        writeBuffer();
    }

    public void createFile(String nameOfResultFile) {
        try (PackageOutputBuffer out = new PackageOutputBuffer(new FileOutputStream(dir + nameOfResultFile).getChannel(), 40)) {
            Queue<FileMerge> filesToMerge = MergeService.getMergeService().stopService();
            ArrayList<PackageInputBuffer> inputBuffers = new ArrayList();
            ArrayList<PackageInputBuffer> inputHeaderBuffers = new ArrayList();
            PriorityQueue<Package> queue = new PriorityQueue<>(numberOfBuffer);

            TreeMap<Package, Integer> map = new TreeMap<>();

            for (FileMerge fileMerge : filesToMerge) {
                inputBuffers.add(new PackageInputBuffer(new FileInputStream(fileMerge.getFile()).getChannel(), 10));
                inputHeaderBuffers.add(new PackageInputBuffer(new FileInputStream(fileMerge.getFileHeader()).getChannel(), 5));
            }

            Integer[] values = new Integer[numberOfBuffer];
            for (int i = 0; i < inputBuffers.size(); i++) {
                values[i] = i;
            }

            for (int i = 0; i < inputBuffers.size(); i++) {
                Header tempHeader = inputHeaderBuffers.get(i).readPackageHeader();
                Package pack = new Package(tempHeader);
                map.put(pack, values[i]);
                queue.add(pack);
            }

            while (queue.peek() != null) {
                Package temp = queue.poll();
                int index = map.get(temp);
                out.writeDataOfPackage(temp.getHeader(), inputBuffers.get(index));

                map.remove(temp);
                Header tempHeader = inputHeaderBuffers.get(index).readPackageHeader();
                temp = new Package(tempHeader);
                if (tempHeader != null) {
                    map.put(temp, values[index]);
                    queue.add(temp);
                }
            }

            for (PackageInputBuffer inputBuffer : inputBuffers) {
                inputBuffer.close();
            }
            for (PackageInputBuffer inputHeaderBuffer : inputHeaderBuffers) {
                inputHeaderBuffer.close();
            }
            for (FileMerge fileMerge : filesToMerge) {
                fileMerge.getFile().delete();
                fileMerge.getFileHeader().delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
    }

    public boolean canAddPackage(long addSize) {
        return (currentSizeOfBuffer + addSize < maxSizeOfBuffer && currentNumberOfPackages < maxNumberOfPackages);
    }
}