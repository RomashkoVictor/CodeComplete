package romashko.by;

import romashko.by.controller.FileServletTest;

import static romashko.by.service.MainService.*;

public class MemoryAndCPUStatistics implements Runnable {
    private static final MemoryAndCPUStatistics statistics = new MemoryAndCPUStatistics();
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

    public static MemoryAndCPUStatistics getMemoryAndCPUStatistics(){
        return statistics;
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
                    LOGGER.info("Current time: " + (System.currentTimeMillis() - startTime));
                    LOGGER.info("Current memory use(MB): " + currentMemoryUse + '\n');
                }
            }
            LOGGER.info("Number of elements: " + FileServletTest.numberOfElements/1_000 + "k");
            LOGGER.info("Total time: " + (System.currentTimeMillis() - startTime));
            LOGGER.info("Time for getting all packages: " + timeForGettingAllPackages);
            LOGGER.info("Max memory use(MB): " + maxMemoryUse + "\n\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
