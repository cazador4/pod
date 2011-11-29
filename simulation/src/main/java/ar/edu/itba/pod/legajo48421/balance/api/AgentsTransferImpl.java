package ar.edu.itba.pod.legajo48421.balance.api;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ar.edu.itba.balance.api.AgentsTransfer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.event.EventInformation;
import ar.edu.itba.event.RemoteEventDispatcher;
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

		host.getRemoteEventDispatcher().getLock().writeLock().lock();
		Set <EventInformation> events = new HashSet<EventInformation>();
		Preconditions.checkNotNull(agents, "Agents can not be null");
		for(NodeAgent agent : agents){
			if(!host.getNodeInformation().equals(agent.node())){
				RemoteEventDispatcher remoteEventDispatcher = host.getRemoteEventDispatcherFor(agent.node());
				if(remoteEventDispatcher!=null)
					events.addAll(remoteEventDispatcher.newEventsFor(host.getNodeInformation()));
			}

			host.getExtendedMultiThreadEventDispatcher().setAgentQueue(agent.agent(), host.getRemoteEventDispatcher().moveQueueFor(agent.agent()));
			
			for(EventInformation event : events){
				try {
					host.getRemoteEventDispatcher().publish(event);

				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			//host.getDispatcher().setAgentQueue(nodeAgent.agent(), dispatcher.moveQueueFor(nodeAgent.agent()));
			System.out.println("agregando al agent " + agent.agent().name() + " al nodo " + host.getNodeInformation());
			host.getSimulation().addAgent(agent.agent());
		}

		host.getRemoteEventDispatcher().getLock().writeLock().unlock();
	}

	@Override
	public int getNumberOfAgents() throws RemoteException {
		return host.getSimulation().agentsRunning();
	}

	@Override
	public List<NodeAgent> stopAndGet(int numberOfAgents) throws RemoteException{
		synchronized(host.getRemoteEventDispatcher().getPendingEvents()){
			List<NodeAgent> result = new ArrayList<NodeAgent>();
			List<Agent> agentsRunning = host.getSimulation().getAgentsRunning();
			for(int i=0; i<numberOfAgents; i++){
				host.getSimulation().remove(agentsRunning.get(i));
				NodeAgent nodeAgent = new NodeAgent(host.getNodeInformation(), agentsRunning.get(i));
				result.add(nodeAgent);
			}
			return result;
		}
	}

}
