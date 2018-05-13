package client;

import server.ServerCommInterface;

/**
 * Manager
 */
public class Manager implements ClientCommInterface {
	public static final int NWORKERS = 4;

	public void publishProblem(byte[] hash, int problemsize) throws Exception {
		// I am the leader!
		// recieved the problem bitchezzz
		// distribute to workers
		// but also work myself!

		// problemsize is the upper bound of the int
		// it should be divided into chunks of
		// ThreadPool pool = new ThreadPool(3);
		// * into subranges eg iif the leader says 0-500 you need to make tasks for
		// 0-125,
		// * 125-250, 250-..., and so on for (int i = 0; i < 5; i++) { Task task = new
		// * Task(i); pool.execute(task); //here each task will be an integer range }
	}

	// main method
	public static void main(String[] args) {

		System.out.println("Client starting...");

		// Initially we have no problem :)
		byte[] problemHash = null;

		// Lookup the server
		// Note: Insert the IP-address or the domain name of the host
		// where your server is running
		ServerCommInterface sci = (ServerCommInterface) Naming.lookup("rmi://actarus.inf.unibz.it/server");

		// Create a communication handler and register it with the server
		// The communication handler is the object that will receive the tasks from the
		// server
		ClientCommHandler cch = new ClientCommHandler();
		System.out.println("Client registers with the server");

		// Note: This is a dull client written for testing purposes
		// This is an example of what a registration can look like
		sci.register("ThatDamnGPU", "javais4pussies", cch);

		/*
		 * 
		 */

		// rest of thigs
	}
}