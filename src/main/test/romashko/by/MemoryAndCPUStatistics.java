package romashko.by;

import romashko.by.service.FileServiceTest;
import romashko.by.service.Service;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class MemoryAndCPUStatistics implements Runnable {
    private boolean running;
    private long startTime;
    private int frequency;
    private boolean onlyResult;
    private Thread thread;
    private long timeForGettingAllPackages;

    public void startStatistics(int frequency, boolean onlyResult) {
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
        try {
            while (running) {
                Thread.sleep(frequency);

                currentMemoryUse = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576;
                if (currentMemoryUse > maxMemoryUse) {
                    maxMemoryUse = currentMemoryUse;
                }
                if (!onlyResult) {
                    Service.LOGGER.debug("Current time: " + (System.currentTimeMillis() - startTime));
                    Service.LOGGER.debug("Current memory use(MB): " + currentMemoryUse + '\n');
                }
            }
            Service.LOGGER.debug("Number of elements: " + FileServiceTest.numberOfElements/1_000 + "k");
            Service.LOGGER.debug("Total time: " + (System.currentTimeMillis() - startTime));
            Service.LOGGER.debug("Time for getting all packages: " + timeForGettingAllPackages);
            Service.LOGGER.debug("Max memory use(MB): " + maxMemoryUse + "\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
