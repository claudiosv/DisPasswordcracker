import java.io.IOException;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Worker {
    private static Worker workerSingleton = null;
    public String currentLeaderIP = null;
    public ArrayList<BackgroundSocket> connectedClients = null;
    public BackgroundSocket currentLeader = null;
    public ConcurrentHashMap<byte[], Integer> hashesMap = null;
    public String currentProblem = null;
    public ServerListener currentServerListener = null;

    private Worker() { }

    public static Worker getInstance( ) {
        if(workerSingleton == null) workerSingleton = new Worker();
        return workerSingleton;
    }

    public void startAsClient()
    {
        try
        {
            Socket serverSocket = new Socket("127.0.0.1", 3333);
            currentLeader = new BackgroundSocket(serverSocket);
            currentLeader.run();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public void startAsServer()
    {
        currentServerListener = new ServerListener(3333);
        currentServerListener.run();
    }

    public void parsePacket(String packet)
    {
        String[] contents = packet.split(" ");

        switch (contents[0]) {
            case "SOLVE": // SOLVE 0957u387r84r7thisisahash98fre98
                // search hashmap
                Worker.getInstance().searchHashes(contents[1]);
            case "RANGE": // e.g. RANGE 500-1000
                // work all integers in the range
                Worker.getInstance().processRange(Integer.parseInt(contents[1]) , Integer.parseInt(contents[2]));
                break;
            case "IPLIST": // IPLIST 127.0.0.1 127.0.0.2
                // if leader, inform the client of the current other members in pool
                // this way, the clients can pick the lowest ip as the new leader
                // the new leader will have to reregister!!!!
            case "SOLVED": // SOLVED 0957u387r84r7thisisahash98fre98 666666
                // only the leader receives this
                System.out.println("The solution to " + contents[2] + " is: " + contents[1]);
            case "STOP": // STOP 0957u387r84r7thisisahash98fre98
                // leader informs workers to stop all work on this hash (since a solution is
                // found)
        }
    }

    public void processRange(int lowerBound, int upperBound)
    {   //upper EXCLUSIVE
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            byte[] hash;
            for (Integer i = lowerBound; i < upperBound; i++){
                hash = digester.digest(i.toString().getBytes());
                hashesMap.put(hash, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    //shouldn't we use Integer instead of int?
    public void sendSolution(int solution, String problem)
    {
        Worker.getInstance().currentLeader.sendRequest("SOLVED" + " " + solution + " " + problem );
    }

    public void searchHashes(String problem)
    {
        // -> currentProblem updated here?
        //simple n search for the correct key
        Integer solution = hashesMap.get(problem.getBytes());
        if(solution != null){
            Worker.getInstance().sendSolution(solution, problem);
        }else {
            //no solution found in my map
            System.out.println("Fok!");
        }
    }
}
