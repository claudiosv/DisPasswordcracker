package nutt;

import java.rmi.Remote;

import nutt.ClientCommInterface;

public interface ServerCommInterface extends Remote {
	
	public void register(String teamName, String teamCode, ClientCommInterface cc) throws Exception;

	public void reregister(String teamName, String teamCode, ClientCommInterface cc) throws Exception;
	
	public void submitSolution(String name, String teamCode, String sol) throws Exception;

	public String getTeamIP(String teamName, String teamCode) throws Exception;

}
