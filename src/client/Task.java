package client;

import java.security.MessageDigest;
import java.util.Arrays;

public class Task implements Runnable {

    private int lowerBound;
    private int upperBound;
    byte[] problemHash;
    MessageDigest md;
    private volatile boolean shutdown;

    public Task(int lowerBound, int upperBound, byte[] problemHash) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.problemHash = problemHash;
        this.shutdown = false;
    }

    public void run() {
        try {
            md = MessageDigest.getInstance("MD5");
            while (!shutdown) {
                for (Integer i = lowerBound; i <= upperBound && !shutdown; i++) {
                    // Calculate their hash
                    byte[] currentHash = md.digest(i.toString().getBytes());
                    // If the calculated hash equals the one given by the server, submit the integer
                    // as solution
                    if (Arrays.equals(currentHash, problemHash)) {
                        // System.out.println("Client submits solution: " + i);
                        // sci.submitSolution("TheCoolTeam", "Cool1234", i.toString());

                        // SOLUTION FOUND!!!
                        this.shutdown();
                        break;
                    }
                }
            }
        }catch (Exception ex){}
        // invoke parent's IM DONE function
    }

    public void shutdown() {
        shutdown = true;
    }
}