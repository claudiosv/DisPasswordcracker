import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundSocket extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Timer awakeCheck = new Timer();
    private String myIP;

    public BackgroundSocket(Socket socket) {
        this.clientSocket = socket;
        //each bgsockets checks each other
        awakeCheck.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkConnection();
                System.out.println("Debug: IPs checked!");
            }
        }, 5000, 5000); // each 5 seconds check connection!
    }

    public void checkConnection(){
        try {
            if(!InetAddress.getByName(getRemoteIP()).isReachable(1000))
                Worker.getInstance().notifyOffline(getRemoteIP());
        } catch (IOException e) { }
    }

    @Override
    public void run() {
        //start Listening than get the ip
        //System.out.println("Remote: " + getRemoteIP() + " ----- Inet: " + getIP() );
        System.out.println("Listening on => " + getRemoteIP());
        listen();
    }

    public String getRemoteIP() {
        return clientSocket.getInetAddress().getHostAddress();
    }
    public String getIP(){
        if(myIP == null)
            try { myIP = InetAddress.getLocalHost().getHostAddress();} catch (UnknownHostException e) {}
        return myIP;
    }

    public void listen() {
        try {
            String inputLine;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                // process inputLine
                Worker.getInstance().parsePacket(inputLine, getRemoteIP());
                // out.println(inputLine);
            }
            in.close();
            clientSocket.close();
            System.out.println("Debug: Socket performed a CLEAN disconnection");
            Worker.getInstance().notifyOffline(getRemoteIP());
        } catch (Exception e){ e.printStackTrace(); /*TODO remove*/ }
    }

    public void sendRequest(String inputLine) {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(inputLine);
            System.out.println("Debug: sent command => " + inputLine);
        } /*catch (ConnectException e) {
            System.out.println("Debug: Offline status detected! IP => " + getRemoteIP());
            Worker.getInstance().notifyOffline(getRemoteIP());
        } */ catch (IOException iEx) {
            iEx.printStackTrace();
        }
    }

    public void dismiss(){
        try {
            awakeCheck.cancel();
            currentThread().join(10); //close the thread
        } catch (InterruptedException e) { }
    }
}
