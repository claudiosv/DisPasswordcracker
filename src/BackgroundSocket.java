import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BackgroundSocket extends Thread {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public BackgroundSocket(Socket socket) {
        this.clientSocket = socket;
    }

    @Override
    public void run() {
        listen();
    }

    public String getRemoteIP() {
        return clientSocket.getRemoteSocketAddress().toString();
    }

    public void listen() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // process inputLine
                Worker.getInstance().parsePacket(inputLine);
                // out.println(inputLine);
            }

            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException iEx) {
            iEx.printStackTrace();
        }
    }

    public void sendRequest(String inputLine) {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(inputLine);
        } catch (IOException iEx) {
            iEx.printStackTrace();
        }
    }

}
