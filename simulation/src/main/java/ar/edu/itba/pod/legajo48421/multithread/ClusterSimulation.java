package ar.edu.itba.pod.legajo48421.multithread;

import static com.google.common.base.Preconditions.checkNotNull;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.balance.api.NotCoordinatorException;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.legajo48421.node.api.Host;
import ar.edu.itba.pod.multithread.EventDispatcher;
import ar.edu.itba.pod.multithread.LocalSimulation;
import ar.edu.itba.pod.time.TimeMapper;

public class ClusterSimulation extends LocalSimulation{

	private Host host;

	public ClusterSimulation(TimeMapper timeMapper, Host host){
		super(timeMapper);
		checkNotNull(timeMapper, "Time mapper cannot be null");
		checkNotNull(host, "Host cannot be null");
		this.host = host;
	}

	public EventDispatcher dispatcher() {
		return host.getExtendedMultiThreadEventDispatcher();
	}


	@Override
	public void add(Agent agent) {
		checkNotNull(agent, "Agent cannot be null");
		NodeInformation coord = host.getAgentsBalancer().getCoordinator();
		NodeAgent nodeAgent = new NodeAgent(host.getNodeInformation(), agent);
		if(host.getNodeInformation().equals(host.getAgentsBalancer().getCoordinator())){
			try {
				host.getAgentsBalancer().addAgentToCluster(nodeAgent);
			} catch (RemoteException e) {
				//e.printStackTrace();
			} catch (NotCoordinatorException e) {
				//e.printStackTrace();
			}
		}
		else{
			try {
				Registry registry =  LocateRegistry.getRegistry(coord.host(), coord.port());
				AgentsBalancer agentsBalancer = (AgentsBalancer)registry.lookup(Node.AGENTS_BALANCER);
				agentsBalancer.addAgentToCluster(nodeAgent);
			} catch (AccessException e) {
				//e.printStackTrace();
			} catch (RemoteException e) {
				//e.printStackTrace();
				try {
					host.getCluster().disconnectFromGroup(coord);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (NotBoundException e1) {
					e1.printStackTrace();
				}
			} catch (NotBoundException e) {
				try {
					host.getCluster().disconnectFromGroup(coord);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				} catch (NotBoundException e1) {
					e1.printStackTrace();
				}
			} catch (NotCoordinatorException e) {
				e.printStackTrace();
			}
		}
	}


	public void addAgent(Agent agent) {
		super.add(agent);
	}
}
