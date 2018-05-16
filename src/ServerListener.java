import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class ServerListener extends Thread {
    private ServerSocket serverSocket;
    private ArrayList<BackgroundSocket> backgroundSockets;
    private int port;

    public ServerListener (int port){
        this.port = port;
        backgroundSockets = new ArrayList<>();
    }

    @Override
    public void run() {
        try{
            serverSocket = new ServerSocket(port);
            while (true)
            {
                BackgroundSocket bs = new BackgroundSocket(serverSocket.accept()); 
                backgroundSockets.add(bs);
                bs.start();
                bs.sendRequest("RANGE 0 100");
            }
        } catch (IOException ex){ex.printStackTrace();}
    }

    public void disconnect() throws IOException {
        serverSocket.close();
    }

    public List<String> getConnectedIPs() //
    {
        List<String> ips = new ArrayList<>();
        for (BackgroundSocket client : backgroundSockets) {
            ips.add(client.getRemoteIP());
        }
        return ips;
    }
}
