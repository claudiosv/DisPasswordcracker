package benchmarks;

import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.TreeMap;

public class HashTree<K, V> extends TreeMap {

    MessageDigest md;
    public HashTree() {
        try {
            md = md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public Object put(Object value) {

        return super.put(md.digest(value.toString().getBytes()), value);
    }
}
