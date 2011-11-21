package ar.edu.itba.pod.legajo48421.balance.api;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import ar.edu.itba.balance.api.AgentsTransfer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.legajo48421.node.api.Host;

import com.google.common.base.Preconditions;

public class AgentsTransferImpl implements AgentsTransfer{

	private Host host;
	
	public AgentsTransferImpl(Host host){
		this.host = host;
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void runAgentsOnNode(List<NodeAgent> agents) throws RemoteException {
		Preconditions.checkNotNull(agents, "Agents can not be null");
			for(NodeAgent agent : agents){
				host.getSimulation().addAgent(agent.agent());
				NodeInformation nodePrev = agent.node();
			}
	}

	@Override
	public int getNumberOfAgents() throws RemoteException {
		return host.getSimulation().agentsRunning();
	}

	@Override
	public List<NodeAgent> stopAndGet(int numberOfAgents)
			throws RemoteException {
		List<NodeAgent> result = new ArrayList<NodeAgent>();
		List<Agent> agentsRunning = host.getSimulation().getAgentsRunning();
		for(int i=0; i<numberOfAgents; i++){
			host.getSimulation().remove(agentsRunning.get(i));
			NodeAgent nodeAgent = new NodeAgent(host.getNodeInformation(), agentsRunning.get(i));
			result.add(nodeAgent);
		}
		// TODO sincronizar eventos! 
		//host.getRemoteEventDispatcher().moveQueueFor(agent)
		return result;
	}

}
