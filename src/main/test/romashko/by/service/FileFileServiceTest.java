package romashko.by.service;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;
import romashko.by.MemoryAndCPUStatistics;
import romashko.by.model.Package;

import java.io.*;
import java.util.Random;

import static romashko.by.service.MainService.*;

public class FileFileServiceTest {
    public static int numberOfElements = 50_000_000;
    private String nameOfInputFile = "in" + numberOfElements + ".txt";
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
        if (shuffle) {
            numbers = getRandomNumbers(numbers);
        }
        try (PackageOutputBuffer out = new PackageOutputBuffer(new FileOutputStream(nameOfInputFile).getChannel(), 4)) {
            LOGGER.info("Start generating file");
            for (int i = 0; i < numberOfElements; i++) {
                Package temp = new Package(numbers[i], (numbers[i] + " abcdefghijklmnopqrstuvwxyz\n").getBytes());
                out.writePackage(temp);
            }
            out.flush();
            LOGGER.info("Cancel generating file");
        } catch (Exception e) {
            LOGGER.error(e);
        }
    }

    public boolean isFileCorrect() {
        int numberOfPackage = 1;
        int character;
        int tempNumber = 0;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(nameOfOutputFile))) {
            character = in.read();
            while (character != -1) {
                while (character >= '0' && character <= '9') {
                    tempNumber = tempNumber * 10 + (character - '0');
                    character = in.read();
                }
                if (tempNumber != numberOfPackage) {
                    return false;
                }
                numberOfPackage++;
                tempNumber = 0;
                while (character != -1 && (character < '0' || character > '9')) {
                    character = in.read();
                }
            }
            if (numberOfPackage != numberOfElements + 1) {
                return false;
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return true;
    }

    @Test
    @Ignore
    public void startTest() throws Exception {
        MemoryAndCPUStatistics statistics = new MemoryAndCPUStatistics();
        File inFile = new File(nameOfInputFile);
        if (!inFile.exists() || inFile.length() == 0) {
            statistics.startStatistics(100, true);
            generateFile(false);
            statistics.endStatistics();
        }
        LOGGER.debug("Start test with number: " + numberOfElements);
        statistics.startStatistics(100, true);
        FileService fileService = new FileService(100_000, 10_000_000);
        try (PackageInputBuffer in = new PackageInputBuffer(new FileInputStream(nameOfInputFile).getChannel(), 4)) {
            Package temp = in.readPackage();
            while (temp != null) {
                fileService.add(temp);
                temp = in.readPackage();
            }
            fileService.closeBuffer();

            statistics.retrievedAllPackage();

            fileService.createFile(nameOfOutputFile);

            statistics.endStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e);
        }
        DiskService.getDiskService().stopService();
        if (!isFileCorrect()) {
            System.out.println("File is incorrect\n");
            LOGGER.error("File is incorrect\n");
        }
    }
}
