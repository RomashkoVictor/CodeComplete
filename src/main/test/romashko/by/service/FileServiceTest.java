package romashko.by.service;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;
import romashko.by.MemoryAndCPUStatistics;
import romashko.by.model.Package;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Random;

public class FileServiceTest {
    private int numberOfElements = 50_000_000;
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
        try (FileChannel out = new FileOutputStream(nameOfInputFile).getChannel()) {
            PackageOutputBuffer outputBuffer = new PackageOutputBuffer();
            for (int i = 0; i < numberOfElements; i++) {
                Package temp = new Package(numbers[i], (numbers[i] + " abcdefghijklmnopqrstuvwxyz\n\r").getBytes());
                outputBuffer.writePackage(temp, out);
            }
            outputBuffer.flush(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isFileCorrect() {
        int numberOfPackage = 1;
        int character = 0;
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
                while(character != -1 && (character < '0' || character > '9')){
                    character = in.read();
                }
            }
            if(numberOfPackage != numberOfElements + 1){
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Test
    @Ignore
    public void startTest() throws Exception {
        MemoryAndCPUStatistics statistics = new MemoryAndCPUStatistics();
        File inFile = new File(nameOfInputFile);
        if (!inFile.exists() || inFile.length() == 0) {
            generateFile(true);
        }
        statistics.startStatistics("stat.txt", 100, true);
        Service service = new Service(100_000, 10_000_000);
        try (FileChannel in = new FileInputStream(nameOfInputFile).getChannel()) {
            PackageInputBuffer inputBuffer = new PackageInputBuffer();
            Package temp = inputBuffer.readPackage(in);
            while (temp != null) {
                service.add(temp);
                temp = inputBuffer.readPackage(in);
            }
//            Package temp = service.readPackage(in);
//            while (temp != null) {
//               // service.add(temp);
//                temp = service.readPackage(in);
//            }
            service.flushBuffer();
            statistics.retrievedAllPackage();
            service.createFile(nameOfOutputFile);
            statistics.endStatistics();
            if (!isFileCorrect()) {
                System.out.println("File has been written wrong!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
