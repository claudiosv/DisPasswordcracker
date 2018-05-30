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

    public BackgroundSocket(Socket socket) {
        this.clientSocket = socket;
        //each bgsockets checks each other
        awakeCheck.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkConnection();
                System.out.println("Debug: IPs checked!");
            }
        }, 0, 5000); // each 5 seconds check connection!
    }

    public void checkConnection(){
        try {
            if(InetAddress.getByName(getIP()).isReachable(100))
                Worker.getInstance().notifyOffline(getIP());
        } catch (IOException e) { }
    }

    @Override
    public void run() {
        System.out.println("Remote: " + getRemoteIP() + " ----- Inet: " + getIP() );
        listen();
    }

    public String getRemoteIP() {
        return clientSocket.getRemoteSocketAddress().toString();
    }
    public String getIP(){return clientSocket.getInetAddress().getHostAddress(); }

    public void listen() {
        try {
            String inputLine;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            while ((inputLine = in.readLine()) != null) {
                // process inputLine
                Worker.getInstance().parsePacket(inputLine, getIP());
                // out.println(inputLine);
            }
            in.close();
            clientSocket.close();
            System.out.println("Debug: Socket performed a CLEAN disconnection");
            Worker.getInstance().notifyOffline(getIP());
        } catch (Exception e){
            //serious stuff here
            e.printStackTrace();
        }
    }

    public void sendRequest(String inputLine) {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(inputLine);
            System.out.println("Debug: sent command => " + inputLine);
        } catch (ConnectException e) {
            System.out.println("Debug: Offline status detected! IP => " + getIP());
            Worker.getInstance().notifyOffline(getIP());
        } catch (IOException iEx) {
            iEx.printStackTrace();
        }
    }
}
