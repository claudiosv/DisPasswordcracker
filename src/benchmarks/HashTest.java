package benchmarks;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class HashTest {

    public static void main(String[] args) {

        MessageDigest md = null;
        HashMap<byte[], Integer> javaHMap = new HashMap<>();
        HashTree<byte[], Integer> customTree = new HashTree<>();
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        byte[] v = new byte[16];
        //Integer i = 4543565;
        final int TIMES = 2000;

        long start = System.nanoTime();
        for (Integer i = 0; i <= TIMES; i++) {
            v = md.digest(i.toString().getBytes());
        }//triple conversion
        long end = System.nanoTime();

        System.out.println("Finished hashing! Total time in ns:");
        System.out.println(end - start);

        Integer x = 12343654;

        start = System.nanoTime();
        javaHMap.put(md.digest(x.toString().getBytes()), x);
        end = System.nanoTime();
        System.out.println("Insert done in ns:");
        System.out.println(end - start);

        start = System.nanoTime();
        customTree.put(x);
        end = System.nanoTime();
        System.out.println("Insert done in ns:");
        System.out.println(end - start);
//        System.out.println("Started fetch benchmark");
//
//        start = System.nanoTime();
//
//        int noway = hashes.get(v);
//
//        end = System.nanoTime();
//
//        System.out.println("Finished fetch! Total time in ns: ");
//        System.out.println(end - start);

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
