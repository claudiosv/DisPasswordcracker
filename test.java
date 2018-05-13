
/**
 * test
 */

import java.util.*;
import java.security.*;

public class test {

    public static void main(String[] args) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            HashMap<Integer, byte[]> mapab = new HashMap<Integer, byte[]>();
            for (Integer i = 0; i < 10000000; i++) {
                byte[] currentHash = md.digest(i.toString().getBytes());
                mapab.put(i, currentHash);
            }
            System.in.read();
        } catch (Exception ex) {

        }
    }

}