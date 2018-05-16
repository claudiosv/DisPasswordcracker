package commInterfaces;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * ClientCommHandler
implements ClientCommInterface */

public class ClientCommHandler extends UnicastRemoteObject implements ClientCommInterface {

	public ClientCommHandler() throws RemoteException {}

	public byte[] currProblem = null;
	int currProblemSize = 0;
	
	@Override
	public void publishProblem(byte[] hash, int problemsize) {
		if (hash==null) System.out.println("Problem is empty!");
		else System.out.println(" Client received new problem of size " + problemsize); 
		currProblem = hash;
		currProblemSize = problemsize;
	}
}