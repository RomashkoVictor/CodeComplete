package romashko.by;

import romashko.by.service.FileServiceTest;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class MemoryAndCPUStatistics implements Runnable {
    private boolean running;
    private long startTime;
    private String nameOfFile;
    private int frequency;
    private boolean onlyResult;
    private Thread thread;
    private long timeForGettingAllPackages;

    public void startStatistics(String nameOfFile, int frequency, boolean onlyResult) {
        this.nameOfFile = nameOfFile;
        startTime = System.currentTimeMillis();
        this.frequency = frequency;
        this.onlyResult = onlyResult;
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void retrievedAllPackage() {
        timeForGettingAllPackages = System.currentTimeMillis() - startTime;
    }

    public void endStatistics() throws InterruptedException {
        running = false;
        thread.join();
    }

    @Override
    public void run() {
        long currentMemoryUse = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
        long maxMemoryUse = currentMemoryUse;
        try (PrintWriter stat = new PrintWriter(new FileOutputStream(nameOfFile, true))) {
            while (running) {
                Thread.sleep(frequency);

                currentMemoryUse = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
                if (currentMemoryUse > maxMemoryUse) {
                    maxMemoryUse = currentMemoryUse;
                }
                if (!onlyResult) {
                    stat.println("Current time: " + (System.currentTimeMillis() - startTime));
                    stat.println("Current memory use(MB): " + currentMemoryUse + '\n');
                }
            }
            stat.println("Number of elements: " + FileServiceTest.numberOfElements/1_000 + "k");
            stat.println("Total time: " + (System.currentTimeMillis() - startTime));
            stat.println("Time for getting all packages: " + timeForGettingAllPackages);
            stat.println("Max memory use(MB): " + maxMemoryUse + '\n');
            stat.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
