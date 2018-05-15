package client;

import javafx.application.Platform;
import server.ServerCommInterface;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager
 */
public class Manager implements ClientCommInterface {

	public static final int NWORKERS = 4;
	protected BackgroundSocket bkgSocket;
	public List<TestThread> connectedClients;

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
		new Manager().start();
	}

	public void start() {


		// Initially we have no problem :)
		byte[] problemHash = null;

		// Lookup the server
		// Note: Insert the IP-address or the domain name of the host
		// where your server is running
		ServerCommInterface sci = null;
		try {
			sci = (ServerCommInterface) Naming.lookup("rmi://actarus.inf.unibz.it/server");
		} catch (Exception e){
			System.out.println(e.getStackTrace());
		}
		// Create a communication handler and register it with the server
		// The communication handler is the object that will receive the tasks from the
		// server
		Manager cch = new Manager();
		System.out.println("Client registers with the server");

		// Note: This is a dull client written for testing purposes
		// This is an example of what a registration can look like
		try{
			sci.register("ThatDamnGPU", "javass", cch);
			String teamIp = sci.getTeamIP("ThatDamnGPU", "javass");

			if(teamIp == null)
			{
				//we have a big problem.
			}
			else
			{
				if(InetAddress.getLocalHost().getHostAddress().toString() == teamIp)
				{
					/*
                      _      ____   ____  _  __        _______   __  __ ______
                     | |    / __ \ / __ \| |/ /     /\|__   __| |  \/  |  ____|
                     | |   | |  | | |  | | ' /     /  \  | |    | \  / | |__
                     | |   | |  | | |  | |  <     / /\ \ | |    | |\/| |  __|
                     | |___| |__| | |__| | . \   / ____ \| |    | |  | | |____
                     |______\____/ \____/|_|\_\ /_/    \_\_|    |_|  |_|______|
            MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNNNmdyshmNMMMMMMMMMMMMMMMNNhdNNNm++ymNMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMNNy+-.``    ./symNNNMMMMMMMMMNNhdNNNm+ohmNMMMMMM
            NNNMMMMMMMMMMMMMMMMMMMMMMMMMMNNds-`             .:smNNMMMMMMMNNhmNNNm+ohmNMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMMMMmo.                    `.+mNNNNNMNmhmNNNm+shmNMNNNNN
            MMMMMMMMMMMMMMMMMMMMMMMMMMMN+`                       `sMMMMMMMMmNMMMMssdNMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMMNs.                         `hMMMMMMMmNMMMMyydMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMNd.`                          .hMMMMMMmNMMMMyhmMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMNo``` ``````     ```````      `oMMMMMMmNMMMMhdmMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMm:```````````````````..``     `sMMMMMMNNMMMMydmMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMN:``````````````..``....``   `-NMMMMMMNNMMMMydmMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMN:``````......````......``````omMMMMMMNNMMMMydNMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMN:```............```....````.:hNMMMMMMNNMMMMydNMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMM/`````...............`` ````.:mMMMMMMNNMMMMydNMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMMo.``-```.````````......`````..dMMMMMMNNMMMMydmMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMM/.`..``.--.`.`.o-``.----...`.yMMMMMMMNNMMMMyhmMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMN:...```-/:-......-----..`..-hMMMMMMMMNNMMMMyhmMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMMh-..``./:-......----::-.-/sNMMMMMMMMMMNMMMMyhmMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMMMd-...----.`...```.-//+:NMMMMMMMMMMMMMmMMMMydmMMMMMMMM
            MMMMMMMMMMMMMMMMMMMMMMMMMMMMs.``````....``````-s/+MMMMMMMMMMMMMMmMMMMydmMMMMMMMM
            MMMMMMMMMMMMMMMMMhdMMMMMMMMMN-``````....``````-/.sMMMMMMMMMMMMMMmMMMMydmMMMMMMMM
            MMMMMMMMMMMMMMMMN-hMMMMMmdMMMo````````...```````./sydMMMMMMMMMMMmMMMMyhmMMMMMMMM
            MMMMMMMMMMMMMMMM+:NMMMN+-+NMMN:````.```.`````````..``.:ohNMMMMMMmMMMMyhmMMMMMMMM
            MMMMMMMMMMMMMMMo.sMMMy-.oNMMMMm.`````  ``    ````````` ```.:ohdddMMMMsydMMMMMMMM
            MMMMMMMMMMMMMMN-.mMd:`-dMMMNmdh-               ````      `````...:+ososdNMMMMMMM
            MMMMMMMMMMMMMMy`:My.`.dhs/.```                  ``           ``.`....---/sdNMMMM
            MMMMMMMMMMMMMm.`//``````````                                `````.....---::+dMMM
            MMMMMMMMMMMMM+`  ````` `````                                ```.`....----:---/yN
            MMMMMMMMMMMMM/`````  ```  ``                                ``..``.--.----.-..--
            MMMMMMMMMMMMo`    `````  ```                                ``.``.--.---.-.....`
            MMMMMMMMMMMo`     `  ``````                                 ````.---.-..........
            MMMMMMMMMMy``        ```````                               `````--...-....`.`...
            MMMMMMMMMm.           `` ```                               ````.--.....`.````...
  _____            __  __    _____          _____ _______       _____ _   _   _   _  ______          __
 |_   _|     /\   |  \/  |  / ____|   /\   |  __ \__   __|/\   |_   _| \ | | | \ | |/ __ \ \        / /
   | |      /  \  | \  / | | |       /  \  | |__) | | |  /  \    | | |  \| | |  \| | |  | \ \  /\  / /
   | |     / /\ \ | |\/| | | |      / /\ \ |  ___/  | | / /\ \   | | | . ` | | . ` | |  | |\ \/  \/ /
  _| |_   / ____ \| |  | | | |____ / ____ \| |      | |/ ____ \ _| |_| |\  | | |\  | |__| | \  /\  /
 |_____| /_/    \_\_|  |_|  \_____/_/    \_\_|      |_/_/    \_\_____|_| \_| |_| \_|\____/   \/  \/
					 */
					connectedClients = new ArrayList<>();
					try {
						ServerSocket serverSocket = new ServerSocket();
						serverSocket.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), 9999));
						while(true)
						{
							TestThread thread = new TestThread(serverSocket.accept());
							connectedClients.add(thread);
							Thread backgroundSocketThread = new Thread(thread);
							backgroundSocketThread.setDaemon(true);
							backgroundSocketThread.start();

						}
					}
					catch (Exception ex){
						ex.printStackTrace();
					}
				}
				else
				{
					//connect to captain
					bkgSocket = new BackgroundSocket(teamIp);
					Thread backgroundSocketThread = new Thread(bkgSocket);
					backgroundSocketThread.setDaemon(true);
					backgroundSocketThread.start();
				}
			}


		} catch (Exception e){
			System.out.println(e.getStackTrace());
		}

		/*
		 * 
		 */

		// rest of thigs
	}


	private class BackgroundSocket implements Runnable { //Client
		private Socket echoSocket;
		private PrintWriter out;
		private String host;

		public BackgroundSocket(String host)
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
			try {
				echoSocket = new Socket(host, 9999);
				out =
						new PrintWriter(echoSocket.getOutputStream(), true);
				BufferedReader in =
						new BufferedReader(
								new InputStreamReader(echoSocket.getInputStream()));
				String fromServer = "";
				while (((fromServer = in.readLine()) != null)) {
					String finalServerString = new String(fromServer);
					Platform.runLater(() -> {
						try {

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					});


				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		public void disconnect() {
			try {
				echoSocket.close();
			}catch (Exception ex)
			{

			}
		}
	}


	private class TestThread implements Runnable //Server
	{

		public Socket clientSocket;

		public TestThread(Socket clientSocket)
		{
			this.clientSocket = clientSocket;
		}

		public void sendMessage(String message)
		{
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(clientSocket.getOutputStream())));
				out.println(message);
				out.flush();
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		public void readMessage()
		{
			try {

				BufferedReader in = new BufferedReader(
						new InputStreamReader(clientSocket.getInputStream()));
				while(clientSocket.isConnected())
				{
					String lineReceived = in.readLine();
					System.out.println(lineReceived);

				}
			} catch (SocketException ex)
			{
				ex.printStackTrace();
			} catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}

		@Override
		public void run() {
			readMessage();
		}
	}
}