import commInterfaces.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.rmi.Naming;

public class Worker {
    private static Worker workerSingleton = null;
    public String currentLeaderIP = null;
    public List<String> connectedIPs = null; //for the clients that remains updated with thw state of the net
    public BackgroundSocket currentLeader = null;
    public ConcurrentHashMap<String, Integer> hashesMap = null;
    public ServerListener currentServerListener = null;
    public List<Interval> initialIntervals = null;
    public int port = 3333;
    private boolean isServer = false;
    public String subnet;
    public Integer problemSize, rangeSize;
    public HashMap<String, Integer> ranges;

    private Worker() {
        hashesMap = new ConcurrentHashMap<>();
    }

    public static Worker getInstance( ) {
        if(workerSingleton == null) workerSingleton = new Worker();
        return workerSingleton;
    }

    public void startAsClient(String subnet)
    {
        this.subnet = subnet;
        //here starts the discovery part
        while (currentLeader == null){
        currentLeader = discoverLeader(this.subnet);
        }
        currentLeader.run();//this run is ok
    }

    public void startAsServer(boolean recover)
    {
        isServer = true;
        currentServerListener = new ServerListener(port);
        currentServerListener.start();
        int guessedProblemSize = 10000000; //this is a guess

        // 0: Lookup the server
        // Note: Insert the IP-address or the domain name of the host
        // where your server is running
        try {
            //ServerCommInterface sci = (ServerCommInterface) Naming.lookup("rmi://actarus.inf.unibz.it/server");

            //get ready to comunicate
            // create new client comm handler
            //ClientCommHandler cch = new ClientCommHandler();

            // 1: registers with the server
            if(recover){
                System.out.println("Debug: reregistered simulation");
                //sci.reregister("ThatDamnGPU", "TDGPU", cch );
                // 2b: check the previous status(last range) and resume
            } else {
                System.out.println("Debug: registered simulation");
                //sci.register("ThatDamnGPU", "TDGPU", cch );
                // 2a: start hashing -> send ranges
                currentServerListener.hashSetup(guessedProblemSize, guessedProblemSize/1000); //divide in 10 chunks

            }

            // 3: wait for problem hash from server
            //while(cch.currProblem == null ){/*wait*/}

            // 4: start searching
            Thread.sleep(5000);
            currentServerListener.shareProblemHash("6F908D8330A81A42A7F9C4120AFBEA5D".getBytes());//(cch.currProblem);

            /*^^^^^ All this part could be implemented differently  cause must be smart now it is suuuper dumb ^^^^^*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parsePacket(String packet, String remoteIP)
    {
        System.out.println("Debug: " + packet);
        String[] contents = packet.split(" ");

        switch (contents[0]) {
            case "SOLVE": // SOLVE 0957u387r84r7thisisahash98fre98
                // search hashmap
                Worker.getInstance().searchHashes(contents[1]);
                break;
            case "RANGE": // e.g. RANGE 12
                // work all integers in the range
                Worker.getInstance().processRange(Integer.parseInt(contents[1]));
                break;
            case "SOLVED": // SOLVED 0957u387r84r7thisisahash98fre98 666666
                // only the leader receives this
                System.out.println("The solution to " + contents[2] + " is: " + contents[1]);

                break;
            case "STOP": // STOP 0957u387r84r7thisisahash98fre98
                // leader informs workers to stop all work on this hash (since a solution is
                // found)
                break;
            case "IPLIST": // IPLIST 127.0.0.1 127.0.0.2
                // if leader, inform the client of the current other members in pool
                // this way, the clients can pick the lowest ip as the new leader
                // the new leader will have to reregister!!!!
                ArrayList<String> newIPList = new ArrayList<>();
                for (int i = 1; i < contents.length; i++) {
                    newIPList.add(contents[i]);
                }
                connectedIPs = newIPList;
                //currentLeader.getIP();
                break;
            case "STATUS":
                //used each time the server commits the start if an activity to update everybody on its status
                String IP = contents[1];
                for (int i = 2; i < contents.length; i++) {
                    ranges.put(IP , Integer.parseInt(contents[i]));
                }
                if(isServer)//if the server than tell the others!
                    currentServerListener.broadcastRequest(packet);

                break;
            case "SETUP": // SETUP problemsize rangesize
                problemSize = Integer.parseInt(contents[1]);
                rangeSize = Integer.parseInt(contents[2]);
                break;
            case "NEXT":
                currentServerListener.sendNextRange(remoteIP);
                break;
        }
    }

    public void notifyOffline(String IP){
        System.out.println("Debug: someone went offline! IP => " + IP);
        if (isServer){ // in case this RPI is server and a client disconnected
            currentServerListener.removeOffline(IP); //it will inform all the others
            //btw we can optimize here removing the step and updating
        } else if (!isServer && IP.equalsIgnoreCase(currentLeaderIP)){ //the server went down!
            System.out.println("Debug: Looking for the next server!");
            connectedIPs.remove(IP);
            currentLeader.dismiss();

            String myIPString = null;
            try { myIPString = InetAddress.getLocalHost().getHostAddress(); } catch (UnknownHostException e) {}

            int myIPNumber = Integer.parseInt(myIPString.split("\\.")[3]); //last 3 digit of my ip

            int minIP = myIPNumber;
            for (String othersIPString: connectedIPs) {
                int othersIPNumber = Integer.parseInt(othersIPString.split("\\.")[3]);
                if(myIPNumber > othersIPNumber)
                    minIP = othersIPNumber;
            }
            System.out.println("Debug: my ip number is => " + myIPNumber + " the min is => " + minIP);
            if(minIP == myIPNumber){
                //this client gets to be the server!
                currentLeader = null; //so hopefully the gc will let us save some RAM
                startAsServer(true);
            }else{
                // waits for the server to start
                try{ Thread.sleep(10000); System.out.println("Debug: Sleeping till new server goes online");} catch (Exception e){}

                currentLeader = discoverLeader(subnet);
                currentLeader.start();
            }
        }
        updateIPList(IP);
    }

    public void updateIPList(String IPtoRemove){
        if(connectedIPs != null) //the server would have this null
            this.connectedIPs.remove(IPtoRemove);
    }

    public BackgroundSocket discoverLeader(String subnet){
        System.out.println("Debug: entered in discovery!");
        int timeout = 10; //timeout in milliseconds to check the connection
        String hostTested = "";
        Socket probableLeaderConnection = null;
        BackgroundSocket probableLeaderSocket = null;
        //String myIP = null; faster w/
    try {
        for (int i = 0; i <= 255; i++) {
            hostTested = subnet + "." + i;
            if (InetAddress.getByName(hostTested).isReachable(timeout)) { //!hostTested.equalsIgnoreCase(myIP) &&  faster w/
                System.out.println("Connecting to: " + hostTested);
                try {
                    probableLeaderConnection = new Socket(hostTested, port);
                    probableLeaderSocket = new BackgroundSocket(probableLeaderConnection);
                    currentLeaderIP = hostTested;
                    return probableLeaderSocket;
                } catch (IOException e) {
                    System.out.println("Debug: Hitted client! IP => " + hostTested );
                }
            }
        }
    } catch (IOException e){ }
        System.out.println("UNABLE TO FIND LEADER");
        return null;
    }

    public void processRange(int rangeNumber)
    {   //upper EXCLUSIVE
        int lowerBound = (problemSize/rangeSize) * rangeNumber;
        int upperBound = lowerBound + rangeSize;
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
        System.out.println("Finished range => " + rangeNumber);

        currentLeader.sendRequest("STATUS " + rangeNumber);
        currentLeader.sendRequest("NEXT");
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
