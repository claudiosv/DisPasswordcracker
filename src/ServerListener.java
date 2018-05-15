import java.io.IOException;
import java.net.ServerSocket;

public class ServerListener extends Thread {
    private ServerSocket serverSocket;

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true)
            new BackgroundSocket(serverSocket.accept()).start();
    }

    public void disconnect() throws IOException {
        serverSocket.close();
    }
}
