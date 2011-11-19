package ar.edu.itba.pod.legajo48421.balance.api;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import ar.edu.itba.balance.api.AgentsTransfer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.pod.legajo48421.node.api.Host;

import com.google.common.base.Preconditions;

public class AgentsTransferImpl implements AgentsTransfer{

	private List<NodeAgent> nodeAgents;
	private Host host;
	
	public AgentsTransferImpl(Host host){
		this.host = host;
		nodeAgents = new ArrayList<NodeAgent>();
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void runAgentsOnNode(List<NodeAgent> agents) throws RemoteException {
		Preconditions.checkNotNull(agents, "Agents can not be null");
			nodeAgents.addAll(agents);
			for(NodeAgent agent : agents)
				host.getSimulation().add(agent.agent());
			
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
		//host.getRemoteEventDispatcher().moveQueueFor(agent)
		return null;
	}

}
