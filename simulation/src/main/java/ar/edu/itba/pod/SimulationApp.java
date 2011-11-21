package ar.edu.itba.pod;

import ar.edu.itba.pod.agent.market.Market;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.Duration;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.event.EventInformation;
import ar.edu.itba.node.Node;
import ar.edu.itba.pod.agent.market.Consumer;
import ar.edu.itba.pod.agent.market.Producer;
import ar.edu.itba.pod.agent.market.Resource;
import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.legajo48421.multithread.ClusterSimulation;
import ar.edu.itba.pod.legajo48421.node.api.Host;
import ar.edu.itba.pod.legajo48421.node.api.Main.Command;
import ar.edu.itba.pod.time.TimeMapper;
import ar.edu.itba.pod.time.TimeMappers;

public class SimulationApp implements Runnable {

	private String[] args; 

	private SimulationApp(String[] args){
		this.args = args;
	}

	public static void main(String[] args) {
		new SimulationApp(args).run();
	}

	@Override
	public void run() {

		TimeMapper timeMapper = TimeMappers.oneSecondEach(Duration.standardHours(6));
		Host host;
		try {
			host = new Host(args[3], Integer.valueOf(args[4]), args[2]);

			host.connect(args[0], Integer.valueOf(args[1]));
			Registry connectedRegistry = LocateRegistry.getRegistry(args[0], Integer.valueOf(args[1]));
			final AgentsBalancer agentsBalancerConnected = (AgentsBalancer) connectedRegistry.lookup(Node.AGENTS_BALANCER);
			ClusterSimulation node = new ClusterSimulation(timeMapper, host);
			host.setSimulation(node);

			try {
				System.out.println("Envio una eleccion");
				agentsBalancerConnected.bullyElection(host.getNodeInformation(), System.currentTimeMillis());
				Thread.sleep(5000);
				if(host.getAgentsBalancer().getIsOk().get()){
					if(host.getAgentsBalancer().getCoordinator()==null){
						long timestamp = System.currentTimeMillis();
						agentsBalancerConnected.bullyCoordinator(host.getNodeInformation(), timestamp);
						host.getAgentsBalancer().bullyCoordinator(host.getNodeInformation(), timestamp);
						//host.getAgentsBalancer().setCoordinator(host.getNodeInformation());
						host.getAgentsBalancer().reloadAndBalanceNodeCountAgentsList();
					}
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	

			Resource gold = new Resource("Mineral", "Gold");
			Resource copper = new Resource("Mineral", "Copper");
			Resource steel = new Resource("Alloy", "Steel");

			//				node.add(new Producer("steel mine 1", steel, Duration.standardDays(1), 5));
			//			node.add(new Producer("copper mine1", copper, Duration.standardDays(1), 10));
			node.add(new Producer("gold mine 1", gold, Duration.standardDays(1), 1));
			//			node.add(new Consumer("copper consumer1", copper, Duration.standardHours(8), 2));
			node.add(new Consumer("gold consumer1", gold, Duration.standardDays(2), 4));

			node.add(new Market("gold market", gold));
			node.add(new Market("cooper market", copper));
			node.add(new Market("steel market", steel));

			//		TransfersWatcher logger = new TransfersWatcher("logger", eventManager);
			//		node.addAgent(logger);
			System.out.println("Starting ...");

			while(true){
				System.out.println("list - close - newevent - events");
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
				try {
					String s = br.readLine();
					switch(Command.toCommand(s)){
					case list:
						System.out.println(host.getCluster().connectedNodes());
						System.out.println(host.getAgentsBalancer().getList());
						break;
					case agents:
						System.out.println("Agentes corriendo aca " + host.getSimulation().agentsRunning());
						break;
					case close:
						System.out.println("Agentes corriendo aca " + host.getSimulation().agentsRunning());
						//host.getCluster().disconnectFromGroup(host.getNodeInformation());
						//System.exit(0);
						break;
					case shutdown:
						Registry reg = LocateRegistry.getRegistry(host.getAgentsBalancer().getCoordinator().host(), host.getAgentsBalancer().getCoordinator().port());
						AgentsBalancer balancer = (AgentsBalancer)reg.lookup(Node.AGENTS_BALANCER);
						
						List<NodeAgent> nodeAgentsToMove = new ArrayList<NodeAgent>();
						for(Agent agent : host.getSimulation().getAgentsRunning()){
							NodeAgent nodeAgent = new NodeAgent(host.getNodeInformation(), agent);
							nodeAgentsToMove.add(nodeAgent);
							
						}
						
						balancer.shutdown(nodeAgentsToMove);
						System.exit(0);
						break;
					case newevent:
						Resource resource = new Resource("Alloy", "Steel");
						NodeAgent nodeAgent = new NodeAgent(host.getNodeInformation(), new Producer("steel mine" + 1, resource, Duration.standardDays(1), 5));
						host.getRemoteEventDispatcher().publish(new EventInformation("New Agent", host.getNodeInformation().id() + System.currentTimeMillis(), nodeAgent.agent()));
						break;
					case coord:
						System.out.println("Coordinator is: " + host.getAgentsBalancer().getCoordinator());
						break;
					case events:
						node.startAndWait(Duration.standardMinutes(10));
						break;
					}
				} 
				catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}


		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		} catch (AlreadyBoundException e1) {
			e1.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		System.out.println("Ending ...");
		//		System.out.println("Total transactions: " + Iterables.size(logger.transferItems()));
	}
}
