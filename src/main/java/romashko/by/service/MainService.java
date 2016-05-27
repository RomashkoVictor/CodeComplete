package romashko.by.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import romashko.by.MemoryAndCPUStatistics;
import romashko.by.model.Package;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MainService {
    private static final MainService MAIN_SERVICE = new MainService();
    public static String dir = System.getProperty("user.home") +  File.separator + "Downloads" + File.separator;
    private String nameOfOutputFile = "out.txt";
    private DiskService diskService;
    private FileService fileService;
    private static int maxNumberOfPackages;
    private int numberOfPackages;
    public final static Logger LOGGER = LogManager.getLogger(FileService.class);
    public static boolean close = false;


    private MainService(){
        maxNumberOfPackages = Integer.MAX_VALUE;
        diskService = DiskService.getDiskService();
        fileService = new FileService(100_000, 20_000_000);
        //MemoryAndCPUStatistics.getMemoryAndCPUStatistics().startStatistics(100, true);
    }

    public static int getMaxNumberOfPackages() {
        return maxNumberOfPackages;
    }

    public static MainService getMainService(){
        return MAIN_SERVICE;
    }

    private void createFile(){
        //MemoryAndCPUStatistics.getMemoryAndCPUStatistics().retrievedAllPackage();
        fileService.closeBuffer();
        fileService.createFile(nameOfOutputFile);
        DiskService.getDiskService().stopService();
//        if(!isFileCorrect()){
//            System.out.println("File is incorrect\n");
//            LOGGER.error("File is incorrect\n");
//        } else{
//            System.out.println("File is correct\n");
//        }
        //MemoryAndCPUStatistics.getMemoryAndCPUStatistics().endStatistics();
    }

    public synchronized void addPackage(Package pack, boolean endPackage){
        if(numberOfPackages < maxNumberOfPackages) {
            if (endPackage) {
                maxNumberOfPackages = pack.getNum();
            }
            numberOfPackages++;
            fileService.add(pack);
            if(numberOfPackages == maxNumberOfPackages){
                createFile();
                close = true;
            }
        }
    }

    public boolean isFileCorrect() {
        int numberOfPackage = 1;
        int character;
        int tempNumber = 0;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(dir + nameOfOutputFile))) {
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
            if (numberOfPackage != maxNumberOfPackages + 1) {
                return false;
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return true;
    }
}
