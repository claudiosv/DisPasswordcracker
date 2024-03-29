import commInterfaces.*;

import java.io.*;
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
    private boolean isServer = false, notAlreadyFound = false;
    public String subnet;
    public Integer problemSize, rangeSize;
    public HashMap<String, Integer> ranges;
    public ClientCommHandler cch;
    public PrintWriter fileCacher;
    private String currentProblem;
    public ArrayList<Integer> cachedRanges = new ArrayList<>();

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
        int guessedProblemSize = 15000000; //this is a guess

        // 0: Lookup the server
        // Note: Insert the IP-address or the domain name of the host
        // where your server is running
        try {
            //ServerCommInterface sci = (ServerCommInterface) Naming.lookup("rmi://actarus.inf.unibz.it/server");

            //get ready to comunicate
            // create new client comm handler
            cch = new ClientCommHandler();

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
            //currentProblem = cch.currProblem
            // 4: start searching
            Thread.sleep(10000);//DEBUG
            currentServerListener.shareProblemHash("1bbd886460827015e5d605ed44252251");//(cch.currProblem);
            // 11 111 111
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
                notAlreadyFound = true;
                Worker.getInstance().searchHashes(contents[1]);
                currentProblem = contents[1];
                break;
            case "RANGE": // e.g. RANGE 12
                // work all integers in the range
                Worker.getInstance().processRange(Integer.parseInt(contents[1]));
                break;
            case "SOLVED": // SOLVED YES/NO 666666 0957u387r84r7thisisahash98fre98
                // only the leader receives this

                if(isServer && contents[1].equalsIgnoreCase("YES") && contents[3].equalsIgnoreCase(currentProblem)){
                    System.out.println("Debug: The solution is: " + contents[2]);
                    currentServerListener.broadcastRequest("STOP " + currentProblem);
                    //cch.publishProblem(contents[2].getBytes(), problemSize);
                }
                break;
            case "STOP": // STOP 0957u387r84r7thisisahash98fre98
                // leader informs workers to stop all work on this hash (since a solution is
                // found)
                if(currentProblem.equalsIgnoreCase(contents[1]))
                    notAlreadyFound = false;
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
        if(hashesMap.size() >= 750000)
            dumpOnFile(rangeNumber);
        currentLeader.sendRequest("NEXT");
        searchHashes(rangeNumber + ""); //look for if problem exist
    }

    public void dumpOnFile(int rangeNumber){
        try {
            fileCacher = new PrintWriter(rangeNumber + "");
            for (String hash: hashesMap.keySet()) {
                fileCacher.println(hash + "," + hashesMap.get(hash));
            }
            fileCacher.close();
            cachedRanges.add(rangeNumber);
            hashesMap.clear();//clear the map
            hashesMap = new ConcurrentHashMap<>(); //binds to a new so gc can do smth
            System.gc(); //perform a GC (hopefully)
        } catch (FileNotFoundException e) { }
    }

    public void sendSolution(String problem, boolean solved, int solution)
    {
        Worker.getInstance().currentLeader.sendRequest("SOLVED" + " " + (solved ? "YES " : "NO ") + solution + " " + problem );
    }

    public void searchHashes(String problem)
    {
        if(notAlreadyFound){
        Integer solution = hashesMap.get(problem);
        if(solution != null){
            Worker.getInstance().sendSolution(problem, true, solution);
        }else {
            solution = lookInFileCache(problem);
            if(solution != null)
                Worker.getInstance().sendSolution(problem, true, solution);
            else
                Worker.getInstance().sendSolution(problem, false, -1);
                currentLeader.sendRequest("NEXT"); //than ask for more
        }
        }
    }
    public Integer lookInFileCache(String problem){
        String filename, line, separator = ",";
        for (Integer range : cachedRanges) {
            if(notAlreadyFound){
            filename = "./" + range;
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(separator);
                    System.out.println("Debug : " + data[0] + " => " + problem);
                    if(data[0].equalsIgnoreCase(problem)){
                        return Integer.parseInt(data[1]);
                    }
                }
            } catch (IOException e) {e.printStackTrace();}
        }
        }
        return null;
    }
}
