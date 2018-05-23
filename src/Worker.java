import commInterfaces.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.Naming;

public class Worker {
    private static Worker workerSingleton = null;
    public String currentLeaderIP = null;
    public List<BackgroundSocket> connectedClients = null;
    public BackgroundSocket currentLeader = null;
    public ConcurrentHashMap<String, Integer> hashesMap = null;
    //public String currentProblem = null; -> saved in ClientCommHandler
    public ServerListener currentServerListener = null;
    public List<Interval> initialIntervals = null;

    private Worker() {
        hashesMap = new ConcurrentHashMap<>();
    }

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
        int initialProblemSize = 10000000; //this is a guess

        // 0: Lookup the server
        // Note: Insert the IP-address or the domain name of the host
        // where your server is running
        try {
            ServerCommInterface sci = (ServerCommInterface) Naming.lookup("rmi://actarus.inf.unibz.it/server");

            //get ready to comunicate
            // create new client comm handler
            ClientCommHandler cch = new ClientCommHandler();

            // 1: registers with the server
            sci.register("ThatDamnGPU", "TDGPU", cch );

            /*----- All this part could be implemented differently  cause must be smart now it is suuuper dumb -----*/
            // 2: start hashing -> send ranges
            // we must keep the current status of the work


            // 3: wait for problem hash from server
            while(cch.currProblem == null ){/*wait*/}

            // 4: start searching
            currentServerListener.shareProblemHash(cch.currProblem);

            /*^^^^^ All this part could be implemented differently  cause must be smart now it is suuuper dumb ^^^^^*/

        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    public void parsePacket(String packet)
    {
        System.out.println("Debug: " + packet);
        String[] contents = packet.split(" ");

        switch (contents[0]) {
            case "SOLVE": // SOLVE 0957u387r84r7thisisahash98fre98
                // search hashmap
                Worker.getInstance().searchHashes(contents[1]);
                break;
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
            long startTime = System.nanoTime();
            for (Integer i = lowerBound; i < upperBound; i++){
                hash = digester.digest(i.toString().getBytes());
                hashesMap.put(ServerListener.byteArrayToHex(hash), i);
            }
            long endTime = System.nanoTime();
            System.out.println("Hashing took: " + (endTime-startTime));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Finished range");

    }
    //shouldn't we use Integer instead of int?
    public void sendSolution(String problem, boolean solved, int solution)
    {
        Worker.getInstance().currentLeader.sendRequest("SOLVED" + " " + (solved ? "YES " : "NO ") + solution + " " + problem );
    }

    public void searchHashes(String problem)
    {
        // -> currentProblem updated here?
        //simple n search for the correct key


        Integer solution = hashesMap.get(problem);
        if(solution != null){
            Worker.getInstance().sendSolution(problem, true, solution);
        }else {
            //no solution found in my map
            Worker.getInstance().sendSolution(problem, false, 0);
        }
    }
}
