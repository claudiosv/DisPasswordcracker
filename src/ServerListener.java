import com.sun.corba.se.spi.orbutil.threadpool.Work;
import nutt.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.*;

public class ServerListener extends Thread {
    private ServerSocket serverSocket;
    private HashMap<String, BackgroundSocket> backgroundSockets; // HashMap<String, BackgroundSocket>
    public Integer problemSize, rangeSize;

    private Stack<Integer> toHash;
    private int port;

    public ServerListener (int port){
        backgroundSockets = new HashMap<>();
        this.port = port;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Debug: server is listening");
                BackgroundSocket bs = new BackgroundSocket(serverSocket.accept());
                backgroundSockets.put(bs.getRemoteIP(), bs); // set the ip as key
                System.out.println("Debug: ip added = " + bs.getRemoteIP());
                bs.start();
                //each time a new client is connected it sends all the IPLIST!
                updateIPs();
                sendSetup(bs.getRemoteIP());
                sendNextRange(bs.getRemoteIP());
                // bs.sendRequest("SOLVE 6F908D8330A81A42A7F9C4120AFBEA5D"); //10000000 "6F908D8330A81A42A7F9C4120AFBEA5D" -> "6579843"
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void sendNextRange(String IP) {
        if (problemSize != null) {
            if (!toHash.empty()) {
                int rangeNumber = toHash.pop();
                routeRequest("RANGE " + rangeNumber, IP);
            } else {
                System.out.println("Debug: finished problemsize!");
            }
        }
    }

    public void hashSetup(int problemSize, int rangeSize){
        Integer rangeNo = 0;
        toHash = new Stack<>();
        for (int i = 0; i < problemSize ; i+= rangeSize) {
            toHash.push(rangeNo);
            rangeNo += rangeSize;
        }
        this.problemSize = problemSize;
        this.rangeSize = rangeSize;
        System.out.println("Debug: Setup done!");
        //keep up with those that connected before the setup was done
        broadcastRequest("SETUP " + problemSize + " " + rangeSize);
        for (String IP : backgroundSockets.keySet()) {
            sendNextRange(IP);
        }
    }
    public void sendSetup(String IP){
        if (problemSize != null) {
        routeRequest("SETUP " + problemSize + " " + rangeSize, IP);
        }
    }
    //extracts form the map the ips
    public List<String> getConnectedIPs()
    {
        return new ArrayList<>(backgroundSockets.keySet());
    }

    //a method to sync the state of work should be implemented(more than one)
    public void removeOffline(String offlineIP){ //called by bgsokect on disconnection
        //get and delete the offline client from the list
        backgroundSockets.get(offlineIP).dismiss();
        backgroundSockets.remove(offlineIP);
        updateIPs();
        //redistribute work could go here!
    }
    //for now it's void but than could return smth to notify about the status
    public void shareProblemHash(String hash){
        while (getConnectedIPs().size() < 2) {}//wait
        broadcastRequest("SOLVE " + hash);
    }

    public void updateIPs(){
        StringBuilder stringBuilder = new StringBuilder("IPLIST ");
        for (String IP: getConnectedIPs()) {
            stringBuilder.append(IP + " ");
        }
        broadcastRequest(stringBuilder.toString());
    }

    //https://stackoverflow.com/a/9855338
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String byteArrayToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public void routeRequest(String inputLine, String clientIP){ //used to route the packet to the right IP
        BackgroundSocket destination = backgroundSockets.get(clientIP);
        destination.sendRequest(inputLine);
    }

    public void broadcastRequest(String inputLine){ //sends the packet to all the connected IPs
        for (BackgroundSocket bs : backgroundSockets.values()) {
            bs.sendRequest(inputLine);
        }
    }

    //debug but could be useful
    public void shareIPs(String clientIP){
        StringBuilder stringBuilder = new StringBuilder("IPLIST ");
        for (String IP: getConnectedIPs()) {
            stringBuilder.append(" " + IP);
        }
        routeRequest(stringBuilder.toString(), clientIP);
    }
}
