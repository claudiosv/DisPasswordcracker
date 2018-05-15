package client;

import javafx.application.Platform;
import server.ServerCommInterface;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Worker
 */
public class Worker implements ClientCommInterface {

	public static final int NWORKERS = 4;
	protected BackgroundClientSocket bkgSocket;
	public List<BackgroundServerSocket> connectedClients;
	private ConcurrentHashMap<byte[], Integer> mainHashmap;


	public void publishProblem(byte[] hash, int problemsize) throws Exception {
		// I am the leader!
		// recieved the problem bitchezzz
		// distribute to workers
		// but also work myself!

		// problem size is the upper bound of the int
		// it should be divided into chunks of
		// ThreadPool pool = new ThreadPool(NWORKERS);
		// * into sub ranges eg iif the leader says 0-500 you need to make tasks for
		// 0-125,
		// * 125-250, 250-..., and so on for (int i = 0; i < 5; i++) { Task task = new
		// * Task(i); pool.execute(task); //here each task will be an integer range }
	}

	// main method
	public static void main(String[] args) {

		System.out.println("Client starting...");
		if (args != null && args.length > 0) {

			new Worker().debugServer();
		} else {
			new Worker().debugClient();
		}
		//new Worker().start();
	}

	public void start() {
		// Initially we have no problem :)
		byte[] problemHash = null;
		mainHashmap = new ConcurrentHashMap<>();
		// Lookup the server
		// Note: Insert the IP-address or the domain name of the host
		// where your server is running
		ServerCommInterface sci = null;
		try {
			sci = (ServerCommInterface) Naming.lookup("rmi://actarus.inf.unibz.it/server");
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
		// Create a communication handler and register it with the server
		// The communication handler is the object that will receive the tasks from the
		// server
		Worker cch = new Worker();
		System.out.println("Client registers with the server");

		// Note: This is a dull client written for testing purposes
		// This is an example of what a registration can look like
		try {
			sci.register("ThatDamnGPU", "javass", cch);
			String teamIp = sci.getTeamIP("ThatDamnGPU", "javass");

			if (teamIp == null) {
				//we have a big problem.
			} else {
				if (InetAddress.getLocalHost().getHostAddress().toString() == teamIp) {
					//from here i know that i'm the leader
					connectedClients = new ArrayList<>();
					try {
						ServerSocket serverSocket = new ServerSocket();
						serverSocket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 9999));
						while (true) {
							BackgroundServerSocket thread = new BackgroundServerSocket(serverSocket.accept());
							connectedClients.add(thread);
							Thread backgroundSocketThread = new Thread(thread);
							backgroundSocketThread.setDaemon(true);
							backgroundSocketThread.start();

						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					//connect to captain
					bkgSocket = new BackgroundClientSocket(teamIp);
					Thread backgroundSocketThread = new Thread(bkgSocket);
					backgroundSocketThread.setDaemon(true);
					backgroundSocketThread.start();
				}
			}


		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}

		/*
		 *
		 */
	}

	public void debugServer() {
		try {
			ServerSocket serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 3333));
			while (true) {
				BackgroundServerSocket thread = new BackgroundServerSocket(serverSocket.accept());
				connectedClients.add(thread);
				Thread backgroundSocketThread = new Thread(thread);
				backgroundSocketThread.setDaemon(true);
				backgroundSocketThread.start();

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void debugClient() {
		//connect to captain
		bkgSocket = new BackgroundClientSocket("127.0.0.1");
		Thread backgroundSocketThread = new Thread(bkgSocket);
		backgroundSocketThread.setDaemon(true);
		backgroundSocketThread.start();
	}

	private class BackgroundClientSocket implements Runnable { //Client
		//private Socket echoSocket;
		private PrintWriter out;
		private String host;

		public BackgroundClientSocket(String host)
		{
			this.host = host;
		}


		public void sendMessage(String message)
		{
			try
			{
				out.println(message);
				out.flush();
			}catch (Exception ex)
			{

			}
		}

		@Override
		public void run() {
			Socket echoSocket = null;
			try {
				echoSocket = new Socket("127.0.0.1", 3333);
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
			try {
				System.out.println("fist");

				System.out.println("second");
				out =
						new PrintWriter(echoSocket.getOutputStream(), true);
				BufferedReader in =
						new BufferedReader(
								new InputStreamReader(echoSocket.getInputStream()));
				String fromServer = "";
				while (((fromServer = in.readLine()) != null)) {
					String finalServerString = new String(fromServer);
					String[] contents = finalServerString.split(" ");

					switch (contents[0]) {
						case "SOLVE": // SOLVE 0957u387r84r7thisisahash98fre98
							// search hashmap
						case "RANGE": // e.g. RANGE 500 1000
							// work all integers in the range
							ThreadPool threadPool = new ThreadPool(NWORKERS);
							for (int i = 0; i < NWORKERS; i++) {
								Task task = new Task(mainHashmap, Integer.parseInt(contents[1]), Integer.parseInt(contents[2]));
								threadPool.execute(task); //here each task will be an integer range }
							}
							break;
						case "CURR": // CURR 500
							// updates the worker on the current pool max so that in case the leader is
							// lost,
							// the client can assume leader position and work from there
						case "MORE": // MORE
							// this is something only the leader can reply to
							// reply error NOT LEADER if mistaken, otherwise send new WORK packet
						case "IPLIST": // IPLIST 127.0.0.1 127.0.0.2
							// if leader, inform the client of the current other members in pool
							// this way, the clients can pick the lowest ip as the new leader
							// the new leader will have to reregister!!!!
						case "SOLVED": // SOLVED 0957u387r84r7thisisahash98fre98 666666
							// only the leader receives this
						case "STOP": // STOP 0957u387r84r7thisisahash98fre98
							// leader informs workers to stop all work on this hash (since a solution is
							// found)
					}
				}
			} catch (Exception ex) {
				System.out.println("fuck");
				ex.printStackTrace();
			}
		}

		public void disconnect() {
			try {
				//echoSocket.close();
			}catch (Exception ex)
			{

			}
		}
	}

	private class BackgroundServerSocket implements Runnable //Server
	{

		public Socket clientSocket;

		public BackgroundServerSocket(Socket clientSocket) {
			this.clientSocket = clientSocket;
		}

		public void sendMessage(String message) {
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(clientSocket.getOutputStream())));
				out.println(message);
				out.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void readMessage() {
			try {

				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				while (clientSocket.isConnected()) {
					String lineReceived = in.readLine();
					System.out.println(lineReceived);

				}
			} catch (SocketException ex) {
				ex.printStackTrace();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void run() {
			//readMessage();
			try {
				while (true) {
					sendMessage("RANGE 0 100");
					Thread.sleep(100);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public void parsePacket(String packet) {
		String[] contents = packet.split(" ");

		switch (contents[0]) {
			case "SOLVE": // SOLVE 0957u387r84r7thisisahash98fre98
				// search hashmap
			case "RANGE": // e.g. RANGE 500-1000
				// work all integers in the range

				break;
			case "CURR": // CURR 500
				// updates the worker on the current pool max so that in case the leader is
				// lost,
				// the client can assume leader position and work from there
			case "MORE": // MORE
				// this is something only the leader can reply to
				// reply error NOT LEADER if mistaken, otherwise send new WORK packet
			case "IPLIST": // IPLIST 127.0.0.1 127.0.0.2
				// if leader, inform the client of the current other members in pool
				// this way, the clients can pick the lowest ip as the new leader
				// the new leader will have to reregister!!!!
			case "SOLVED": // SOLVED 0957u387r84r7thisisahash98fre98 666666
				// only the leader receives this
			case "STOP": // STOP 0957u387r84r7thisisahash98fre98
				// leader informs workers to stop all work on this hash (since a solution is
				// found)
		}

	}
}