package ar.edu.itba.pod.legajo48421.balance.api;

import java.rmi.RemoteException;
import java.util.List;

import ar.edu.itba.balance.api.AgentsTransfer;
import ar.edu.itba.balance.api.NodeAgent;

public class AgentsTransferImpl implements AgentsTransfer{

	private List<NodeAgent> nodeAgents;
	
	@Override
	public void runAgentsOnNode(List<NodeAgent> agents) throws RemoteException {
		if(agents!=null)
			nodeAgents = agents;
			
		//TODO RUN
	}

	@Override
	public int getNumberOfAgents() throws RemoteException {
		if(nodeAgents==null)
			return 0;
		return nodeAgents.size();
	}

	@Override
	public List<NodeAgent> stopAndGet(int numberOfAgents)
			throws RemoteException {
		// TODO 
		return null;
	}

}
