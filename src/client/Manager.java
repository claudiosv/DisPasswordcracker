package client;

import server.ServerCommInterface;
import java.net.Socket;
import java.rmi.Naming;

/**
 * Manager
 */
public class Manager implements ClientCommInterface {

	public static final int NWORKERS = 4;
	protected BackgroundSocket b_socket = new Ba

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

		bkgSocket = new BackgroundSocket();
		Thread backgroundSocketThread = new Thread(bkgSocket);
		backgroundSocketThread.setDaemon(true);
		backgroundSocketThread.start();

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
		} catch (Exception e){
			System.out.println(e.getStackTrace());
		}

		/*
		 * 
		 */

		// rest of thigs
	}
}