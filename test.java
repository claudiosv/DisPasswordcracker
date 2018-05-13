
/**
 * test
 */

import java.util.*;
import java.security.*;

public class test {
    public HashMap<int, byte[]> map;

    public static void main(String[] args) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        map = new HashMap<int, byte[]>;
        for (int i = 0; i < 10000000; i++) {
            byte[] currentHash = md.digest(i.toString().getBytes());
            map.add(i, currentHash);
        }
        System.in.read();
    }

}