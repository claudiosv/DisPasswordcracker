
/**
 * test
 */

import java.util.*;
import java.security.*;

public class test {

    public static void main(String[] args) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            HashMap<byte[], Integer> mapab = new HashMap<>();
            byte[] currentHash = new byte[32];            
            for (Integer i = 0; i < 2500000; i++) {
                currentHash = md.digest(i.toString().getBytes());
                
                mapab.put(currentHash, i);
            }
            System.out.println("Finished hashing");
            long start = System.nanoTime();
            int noway = mapab.get(currentHash);
            long end = System.nanoTime();
            System.out.println("No way: " + noway);
            System.out.println(start - end);
            //System.out.println(end);
            System.in.read();
        } catch (Exception ex) {

        }
    }

}