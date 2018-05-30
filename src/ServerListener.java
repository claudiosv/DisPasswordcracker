import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ServerListener extends Thread {
    private ServerSocket serverSocket;
    private HashMap<String, BackgroundSocket> backgroundSockets; // HashMap<String, BackgroundSocket>
    private int port;

    public ServerListener (int port){
        this.port = port;
        backgroundSockets = new HashMap<>();
    }

    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(port);
            while (true)
            {
                BackgroundSocket bs = new BackgroundSocket(serverSocket.accept()); 
                backgroundSockets.put(bs.getIP(), bs); // set the ip as key
                bs.start();
                bs.sendRequest("RANGE 0 10000000");
               // bs.sendRequest("SOLVE 6F908D8330A81A42A7F9C4120AFBEA5D"); //10000000 "6F908D8330A81A42A7F9C4120AFBEA5D" -> "6579843"
            }
        } catch (IOException ex){ex.printStackTrace();}
    }

    public void disconnect() throws IOException {
        serverSocket.close();
    }

    public List<String> getConnectedIPs()
    {
        return new ArrayList<>(backgroundSockets.keySet());
    }

    //a method to sync the state of work should be implemented(more than one)
    public static void notifyOffline(String offlineIP){ //called by bgsokect on disconnection

    }
    //for now it's void but than could return smth to notify about the status
    public void shareProblemHash(byte[] hash){
        broadcastRequest("SOLVE " + byteArrayToHex(hash));
    }

    public void updateIPs(){
        broadcastRequest("UPDATE begin");
        for (String IP: getConnectedIPs()) {
            broadcastRequest("UPDATE client " + IP);
        }
        broadcastRequest("UPDATE end");
    }

    public void shareIPs(String clientIP){
        routeRequest("UPDATE begin" , clientIP);
        for (String IP: getConnectedIPs()) {
            if(!IP.equalsIgnoreCase(clientIP)) //share all other than the requesting client
                routeRequest("UPDATE client " + IP, clientIP);
        }
        routeRequest("UPDATE end" , clientIP);
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
}
