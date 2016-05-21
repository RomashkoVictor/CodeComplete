package romashko.by.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import romashko.by.model.Package;

public class MainService {
    private static final MainService MAIN_SERVICE = new MainService();
    public static String dir = "C:\\Users\\Victor\\Git\\CodeComplete\\";
    private DiskService diskService;
    private FileService fileService;
    private int maxNumberOfPackages;
    private int numberOfPackages;
    private String nameOfOutputFile = "out.txt";
    public final static Logger LOGGER = LogManager.getLogger(FileService.class);
    public static boolean close = false;

    private MainService(){
        maxNumberOfPackages = Integer.MAX_VALUE;
        diskService = DiskService.getDiskService();
        fileService = new FileService(100_000, 10_000_000);
    }

    public int getMaxNumberOfPackages() {
        return maxNumberOfPackages;
    }

    public void setMaxNumberOfPackages(int maxNumberOfPackages) {
        this.maxNumberOfPackages = maxNumberOfPackages;
    }

    public static MainService getMainService(){
        return MAIN_SERVICE;
    }

    private void createFile(){
        fileService.closeBuffer();
        fileService.createFile(nameOfOutputFile);
        DiskService.getDiskService().stopService();
    }

    public synchronized void addPackage(Package pack, boolean endPackage){
        System.out.println(pack.getNum());
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
}
