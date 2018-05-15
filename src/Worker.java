import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Worker {
    private static Worker workerSingleton = null;
    public String currentLeaderIP = null;
    public ArrayList<BackgroundSocket> connectedClients = null;
    public BackgroundSocket currentLeader = null;
    public ConcurrentHashMap<byte[], Integer> hashesMap = null;
    public String currentProblem = null;

    private Worker() { }

    public static Worker getInstance( ) {
        if(workerSingleton == null) workerSingleton = new Worker();
        return workerSingleton;
    }

    public void startAsClient()
    {

    }

    public void startAsServer()
    {

    }

    public void parsePacket(String packet)
    {

    }

    public void processRange(int lowerBound, int upperBound)
    {

    }

    public void sendSolution(int solution, String problem)
    {}

    public void searchHashes(String problem)
    {

    }
}
