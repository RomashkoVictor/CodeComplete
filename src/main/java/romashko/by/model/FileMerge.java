package romashko.by.model;

import romashko.by.service.DiskService;
import romashko.by.service.MergeService;

import java.io.File;

public class FileMerge implements Comparable{
    private File file;
    private File fileHeader;
    private int generation;

    public FileMerge(File file) {
        this.file = file;
        this.fileHeader = new File(file.toString() + ".header");
        generation = 1;
    }

    public File getFileHeader() {
        return fileHeader;
    }

    public void setFileHeader(File fileHeader) {
        this.fileHeader = fileHeader;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    @Override
    public int compareTo(Object o) {
        return this.generation- ((FileMerge)o).generation;
    }
}
