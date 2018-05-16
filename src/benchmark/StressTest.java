import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class StressTest {

    public static PrintWriter LOGGER = null;

    public static void main(String[] args){
        try {
            //logger init
            LOGGER = new PrintWriter("benchmark_data.txt");
            if (args.length == 0){
                System.out.println("Must specify a test to run:\n[hash <problem size>, map <io number bound>, thread <number of max threads>, heavy <number of max threads>]");
                System.exit(-1);
            }else{
                for (int i = 0; i< args.length; i++) {
                    String arg = args[i];
                    if(arg.equalsIgnoreCase("hash")) {
                        //get problem size
                        try {
                            hashTest(Integer.parseInt(args[i + 1]) >= 100 ? Integer.parseInt(args[i + 1]) : 100);
                        } catch (Exception nex) {
                            hashTest(100);
                        }
                    }else if(arg.equalsIgnoreCase("map")){
                        try {
                            mapTest(Integer.parseInt(args[i + 1]) >= 10 ? Integer.parseInt(args[i + 1]) : 10);
                        } catch (Exception nex) {
                            hashTest(10);
                        }
                    } else if(arg.equalsIgnoreCase("thread")){
                        try {
                            threadTest(Integer.parseInt(args[i + 1]) >= 1 ? Integer.parseInt(args[i + 1]) : 1);
                        } catch (Exception nex) {
                            threadTest(1);
                        }
                    }else if(arg.equalsIgnoreCase("heavy")){
                        try {
                            heavyThreadTest(Integer.parseInt(args[i + 1]) >= 1 ? Integer.parseInt(args[i + 1]) : 1);
                        } catch (Exception nex) {
                            heavyThreadTest(1);
                        }
                    }else{
                        //handle someway
                    }
                }
            }
            LOGGER.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void hashTest(int problemSize){
        LOGGER.println("Starting hash test...");
        LOGGER.println("Problem size is:" + problemSize);

        byte[] v = new byte[16];
        long start,end;
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            LOGGER.println("Setup done! Starting hashing...");
        } catch(NoSuchAlgorithmException e){
            LOGGER.println("No such algorithm");
            System.exit(-1);
        }
        for (int j = 1; j <= 100; j++) {
            start = System.nanoTime();
            for (Integer i = 0; i <= ((problemSize*j)/100); i++) {
                v = messageDigest.digest(i.toString().getBytes());
            }
            end = System.nanoTime();
            LOGGER.println((end - start) + "");
        }
        LOGGER.println("Hash test done!\n");
    }

    public static void mapTest(int ioNumber){
        LOGGER.println("Starting map I/O test...");
        LOGGER.println("I/O number is:" + ioNumber);

        long start,end;
        Integer v = new Integer("12982430284");
        byte[] k = "3f264182bef771652826ceafa0d6e4d7".getBytes(); //pre computed hash

        LOGGER.println("Creating new map...");
        start = System.nanoTime();
        ConcurrentHashMap<byte[], Integer> testMap = new ConcurrentHashMap<>();
        end = System.nanoTime();
        LOGGER.println("New map ready in: ");
        LOGGER.println((end - start) + "");

        LOGGER.println("Setup done! Starting io operations...");

        LOGGER.println("Insert operations...");
        for (int j = 0; j < ioNumber; j++) {
            start = System.nanoTime();
            testMap.put(k, v);
            end = System.nanoTime();
            LOGGER.println((end - start) + "");
            testMap.clear();
        }
        LOGGER.println("Fetch perations...");
        testMap.put(k, v);
        for (int j = 0; j < ioNumber; j++) {
            start = System.nanoTime();
            testMap.get(k);
            end = System.nanoTime();
            LOGGER.println((end - start) + "");
        }
        LOGGER.println("Map I/O test done!\n");

    }

    public static void threadTest(int threadNumber){
        LOGGER.println("Starting thread test...");
        LOGGER.println("Max thread number: " + threadNumber);

        long start,end;
        ArrayList<TestThread> threadArrayList = new ArrayList<>();
        LOGGER.println("Starting creating threads...");

        for (int i = 0; i < threadNumber; i++) {
            start = System.nanoTime();
            TestThread testThread = new TestThread();
            testThread.start();
            end = System.nanoTime();
            LOGGER.println((end - start) + "");
            threadArrayList.add(testThread);
        }
        LOGGER.println("Ended creating threads...");
        LOGGER.println("Started closing threads...");

        for (int i = 0; i < threadNumber ; i++) {
            TestThread testThread = threadArrayList.get(i);
            start = System.nanoTime();
            testThread.halt();
            end = System.nanoTime();
            LOGGER.println((end - start) + "");
        }

        threadArrayList.clear();
        LOGGER.println("Ended closing threads...");

        LOGGER.println("Thread test done!\n");
    }
    public static void heavyThreadTest(int threadNumber){
        LOGGER.println("Starting heavy thread test...");
        LOGGER.println("All the numbers in this test are in MILLISECONDS!");
        LOGGER.println("Max thread number: " + threadNumber);

        long start,end;
        ArrayList<TestHeavyThread> threadArrayList = new ArrayList<>();
        LOGGER.println("Starting creating heavy threads...");

        start = System.currentTimeMillis();
        for (int i = 0; i < threadNumber; i++) {
            TestHeavyThread testThread = new TestHeavyThread();
            testThread.start();
            threadArrayList.add(testThread);
        }
        end = System.currentTimeMillis();
        LOGGER.println("Creating "+ threadNumber +" heavy threads took: " + (end - start));

        LOGGER.println("Started closing heavy threads...");
        start = System.currentTimeMillis();
        for (int i = 0; i < threadNumber; i++) {
            TestHeavyThread currentThread = threadArrayList.get(i);
            try {
                currentThread.join();
                LOGGER.println(""+ currentThread.getTime());
            } catch (InterruptedException e) {
                LOGGER.println(""+ currentThread.getTime());
            }
        }
        end = System.currentTimeMillis();
        LOGGER.println("All heavy threads closed! It toked: " + (end - start));

        LOGGER.println("Heavy thread test done!\n");
    }
}
