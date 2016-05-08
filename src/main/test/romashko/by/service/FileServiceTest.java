package romashko.by.service;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;
import romashko.by.MemoryAndCPUStatistics;
import romashko.by.model.Package;

import java.io.*;
import java.util.Random;

public class FileServiceTest {
    private int numberOfElements = 10_000_000;
    private String nameOfInputFile = "in.txt";
    private String nameOfOutputFile = "out.txt";

    public static int[] getRandomNumbers(int[] numbers) {
        Random random = new Random();
        for (int i = numbers.length - 1, j; i >= 0; --i) {
            j = random.nextInt(i + 1);
            int number = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = number;
        }
        return numbers;
    }

    public void generateFile(boolean shuffle) {
        int[] numbers = new int[numberOfElements];
        for (int i = 0; i < numberOfElements; i++) {
            numbers[i] = i + 1;
        }
        if(shuffle) {
            numbers = getRandomNumbers(numbers);
        }
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(nameOfInputFile))) {
            for (int i = 0; i < numberOfElements; i++) {
                Package temp = new Package(numbers[i], (numbers[i] + " abcdefghijklmnopqrstuvwxyz\n\r").getBytes());
                Service.writePackage(temp, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    @Ignore
    public void startTest() throws Exception {
        MemoryAndCPUStatistics statistics = new MemoryAndCPUStatistics();
        File inFile = new File(nameOfInputFile);
        if(!inFile.exists() || inFile.length() == 0){
            generateFile(true);
        }
        statistics.startStatistics("stat.txt", 100, true);
        Service service = new Service(100_000, 10_000_000);
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(nameOfInputFile))) {
            Package temp = Service.readPackage(in);
            while (temp != null) {
                service.add(temp);
                temp = Service.readPackage(in);
            }
            service.flushBuffer();
            service.createFile(nameOfOutputFile);
            statistics.endStatistics();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
