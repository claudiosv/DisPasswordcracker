import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BackgroundSocket extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean discovery = false;
    private int timeout;


    public BackgroundSocket(Socket socket) {
        this.clientSocket = socket;
    }

    //intentional overload
//    public BackgroundSocket(Socket socket, boolean discoveryState, int timeout) {
//        this.clientSocket = socket;
//        this.discovery = discoveryState;
//        this.timeout = timeout;
//    }

    @Override
    public void run() {
        System.out.println("Remote: " + getRemoteIP() + " ----- Inet: " + getIP() );
        if(discovery){
            discoveryProcedure();
        } else {
            listen();
        }
    }

    public String getRemoteIP() {
        return clientSocket.getRemoteSocketAddress().toString();
    }
    public String getIP(){return clientSocket.getInetAddress().getHostAddress(); }

    public void listen() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // process inputLine
                Worker.getInstance().parsePacket(inputLine, getIP());
                // out.println(inputLine);
            }

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException iEx) {
            if (iEx.getMessage().equals("Connection reset")) {
                System.out.println("Socket disconnected");

            } else {
                iEx.printStackTrace();
            }
        }
    }
    public void discoveryProcedure(){

    }
    public void sendRequest(String inputLine) {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(inputLine);
            System.out.println(inputLine);
        } catch (IOException iEx) {
            iEx.printStackTrace();
        }
    }

}
