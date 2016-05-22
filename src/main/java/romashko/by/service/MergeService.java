package romashko.by.service;

import romashko.by.model.FileMerge;
import romashko.by.model.Header;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static romashko.by.service.MainService.LOGGER;

public class MergeService implements Runnable {
    private static final Queue<FileMerge> filesToMerge = new ConcurrentLinkedQueue<>();
    private static final PriorityQueue<FileMerge> fileMerges = new PriorityQueue<>();
    private static final MergeService MERGE_SERVICE = new MergeService();
    private Thread thread;
    private boolean running = false;

    private MergeService() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public static MergeService getMergeService() {
        return MERGE_SERVICE;
    }

    public static void addMergeFile(FileMerge file) {
        filesToMerge.add(file);
    }

    public Queue<FileMerge> stopService() {
        try {
            running = false;
            synchronized (this) {
                this.notify();
            }
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return filesToMerge;
    }

    public void doMerge(FileMerge first, FileMerge second, FileMerge outFile) {
        try (PackageInputBuffer inFirst = new PackageInputBuffer(new FileInputStream(first.getFile()).getChannel(), 10);
             PackageInputBuffer inFirstHeader = new PackageInputBuffer(new FileInputStream(first.getFileHeader()).getChannel(), 5);
             PackageInputBuffer inSecond = new PackageInputBuffer(new FileInputStream(second.getFile()).getChannel(), 10);
             PackageInputBuffer inSecondHeader = new PackageInputBuffer(new FileInputStream(second.getFileHeader()).getChannel(), 5);
             PackageOutputBuffer out = new PackageOutputBuffer(new FileOutputStream(outFile.getFile()).getChannel(), 20);
             PackageOutputBuffer outHeader = new PackageOutputBuffer(new FileOutputStream(outFile.getFileHeader()).getChannel(), 10)) {

            Header firstHeader = inFirstHeader.readPackageHeader();
            Header secondHeader = inSecondHeader.readPackageHeader();
            Header prev = null;
            while (firstHeader != null && secondHeader != null) {
                if (firstHeader.getStartIndex() < secondHeader.getStartIndex()) {
                    out.writeDataOfPackage(firstHeader, inFirst);
                    prev = addToCluster(prev, firstHeader, outHeader);
                    firstHeader = inFirstHeader.readPackageHeader();
                } else {
                    out.writeDataOfPackage(secondHeader, inSecond);
                    prev = addToCluster(prev, secondHeader, outHeader);
                    secondHeader = inSecondHeader.readPackageHeader();
                }
            }
            while (firstHeader != null) {
                out.writeDataOfPackage(firstHeader, inFirst);
                prev = addToCluster(prev, firstHeader, outHeader);
                firstHeader = inFirstHeader.readPackageHeader();
            }
            while (secondHeader != null) {
                out.writeDataOfPackage(secondHeader, inSecond);
                prev = addToCluster(prev, secondHeader, outHeader);
                secondHeader = inSecondHeader.readPackageHeader();
            }
            if (prev != null) {
                outHeader.writePackageHeader(prev);
            }

        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
        first.getFile().delete();
        second.getFile().delete();
        first.getFileHeader().delete();
        second.getFileHeader().delete();
    }

    public static Header addToCluster(Header prev, Header current, PackageOutputBuffer outHeader) {
        if (prev == null) {
            return current;
        } else {
            if (prev.getEndIndex() + 1 == current.getStartIndex()) {
                prev.setEndIndex(current.getEndIndex());
                prev.setLength(prev.getLength() + current.getLength());
                return prev;
            } else {
                outHeader.writePackageHeader(prev);
                return current;
            }
        }
    }

    @Override
    public void run() {
        try {
            while (running) {
                while (!filesToMerge.isEmpty()) {
                    fileMerges.add(filesToMerge.poll());
                }
                if (fileMerges.size() > 1) {
                    FileMerge first;
                    FileMerge second;
                    FileMerge fileOut;
                    first = fileMerges.poll();
                    second = fileMerges.poll();
                    fileOut = new FileMerge(new File(FileService.getName()));
                    fileOut.setGeneration(first.getGeneration() + second.getGeneration());
                    fileMerges.add(fileOut);
                    doMerge(first, second, fileOut);
                }
                synchronized (this) {
                    if (running) {
                        this.wait();
                    }
                }
            }
            while (!fileMerges.isEmpty()) {
                filesToMerge.add(fileMerges.poll());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
    }
}
