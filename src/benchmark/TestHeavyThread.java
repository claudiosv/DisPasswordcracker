package benchmark;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;

public class TestHeavyThread extends Thread {
    public long start,end;
    @Override
    public void run() {
        start = System.currentTimeMillis();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            ConcurrentHashMap<byte[], Integer> mapab = new ConcurrentHashMap<>();
            byte[] currentHash;
            for (Integer i = 0; i < 1000000/*0*/; i++) {
                currentHash = md.digest(i.toString().getBytes());
                mapab.put(currentHash, i);
            }
        } catch (Exception ex) {}
        end = System.currentTimeMillis();
    }

    public long getTime(){
        return(end - start);
    }
}