package romashko.by.controller;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import romashko.by.MemoryAndCPUStatistics;
import romashko.by.service.MainService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Random;
import java.util.Base64;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static romashko.by.service.MainService.LOGGER;

public class FileServletTest {

    public static int numberOfElements = 10_000;
    private StringWriter stringWriter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private PrintWriter writer;

    @Before
    public void setUp() throws Exception {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);

        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    public void sendPackageTest() throws Exception {
        int[] numbers = getNumbers(numberOfElements, true);
        numbers = getRandomNumbers(numbers);

        LOGGER.debug("Start sendPackageTest with number: " + numberOfElements);
        MemoryAndCPUStatistics.getMemoryAndCPUStatistics().startStatistics(100, true);
        int count =0;
        for (int number : numbers) {
            when(request.getParameter("num")).thenReturn(String.valueOf(number));

            String data = number + getData();
            String dataBase64 = new String(Base64.getEncoder().encode(data.getBytes()));
            when(request.getParameter("checksum")).thenReturn(DigestUtils.md5Hex(data));
            when(request.getParameter("data")).thenReturn(dataBase64);

            if (number == numberOfElements - 1) {
                when(request.getParameter("isLast")).thenReturn("true");
            } else {
                when(request.getParameter("isLast")).thenReturn("false");
            }

            new FileServlet().doPost(request, response);
            writer.flush();
            assertTrue(stringWriter.toString().contains("OK"));
            count++;
            if(count % 1000==0){
                System.out.println(count);
            }
        }
        try {
            synchronized (this) {
                this.notify();
            }
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MemoryAndCPUStatistics.getMemoryAndCPUStatistics().retrievedAllPackage();
        MemoryAndCPUStatistics.getMemoryAndCPUStatistics().endStatistics();
    }

    @Test
    public void isFileCorrect() {
        int numberOfPackage = 1;
        int character;
        int tempNumber = 0;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream("out.txt"))) {
            character = in.read();
            while (character != -1) {
                while (character >= '0' && character <= '9') {
                    tempNumber = tempNumber * 10 + (character - '0');
                    character = in.read();
                }
                if (tempNumber != numberOfPackage) {
                    System.out.println("File is incorrect\n");
                    LOGGER.error("File is incorrect\n");
                }
                numberOfPackage++;
                tempNumber = 0;
                while (character != -1 && (character < '0' || character > '9')) {
                    character = in.read();
                }
            }
            if (numberOfPackage != numberOfElements + 1) {
                System.out.println("File is incorrect\n");
                LOGGER.error("File is incorrect\n");
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
    }

    public static String getData() {
        return " abcdefghijklmnopqrstuvwxyz\n";
    }

    private static int[] getRandomNumbers(int[] numbers) {
        Random random = new Random();
        for (int i = numbers.length - 1, j; i >= 0; --i) {
            j = random.nextInt(i + 1);
            int number = numbers[i];
            numbers[i] = numbers[j];
            numbers[j] = number;
        }
        return numbers;
    }

    public static int[] getNumbers(int amount, boolean shuffle) {
        int[] numbers = new int[amount];
        for (int i = 0 ; i < numbers.length; ++i) {
            numbers[i] = i;
        }
        if (shuffle) {
            numbers = getRandomNumbers(numbers);
        }
        return numbers;
    }
}