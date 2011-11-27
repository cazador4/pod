package ar.edu.itba.pod.legajo48421.node.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.Duration;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.balance.api.NotCoordinatorException;
import ar.edu.itba.event.EventInformation;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;
import ar.edu.itba.node.api.NodeStatistics;
import ar.edu.itba.node.api.StatisticReports;
import ar.edu.itba.pod.agent.market.AgentState;
import ar.edu.itba.pod.agent.market.Market;
import ar.edu.itba.pod.agent.market.Producer;
import ar.edu.itba.pod.agent.market.Resource;
import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.legajo48421.multithread.ClusterSimulation;
import ar.edu.itba.pod.thread.CleanableThread;
import ar.edu.itba.pod.time.TimeMapper;
import ar.edu.itba.pod.time.TimeMappers;

public class Main {
	//Main Server
	public static void main(String[] args){
		//CLUSTER MAIN
		if(args.length==3){
			//CADA NODO TIENE QUE SABER QUIEN ES EL NODO COORDINADOR.
			try {
				final Host host = new Host(args[0], Integer.valueOf(args[1]), args[2]);
				//Thread.sleep(5000);
				host.getAgentsBalancer().bullyElection(host.getNodeInformation(), System.currentTimeMillis());
				host.getCluster().createGroup();

				//host.getAgentsBalancer().bullyCoordinator(host.getNodeInformation(), System.currentTimeMillis());
				
				Resource gold = new Resource("Mineral", "Gold");
				//host.getCluster().createGroup();
				ClusterSimulation node = new ClusterSimulation(TimeMappers.oneSecondEach(Duration.standardHours(6)), host);
				host.setSimulation(node);
				node.add(new Producer(host.getNodeInformation().port() + " - gold mine", gold, Duration.standardHours(2), 5));
				

				Resource steel = new Resource("Alloy", "Steel");
				TimeMapper timeMapper = TimeMappers.oneSecondEach(Duration.standardHours(6));
				//ClusterSimulation node = new ClusterSimulation(timeMapper, host);
				host.setSimulation(node);
				//for (int i = 0; i < 1; i++) {
				node.add(new Market("steel market", steel));
				//node.add(new Producer("steel consumer1", steel, Duration.standardDays(3), 2));
				//}
				/*try {
					node.startAndWait(Duration.standardSeconds(5000));
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}*/
				System.out.println("Server");
				while(true){
					System.out.println("list - close - newevent - events");
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
					try {
						String s = br.readLine();
						switch(Command.toCommand(s)){
						case list:
							System.out.println(host.getCluster().connectedNodes());
							break;
						case agents:
							System.out.println("Agentes corriendo aca " + host.getSimulation().agentsRunning());
							break;
						case close:
							System.out.println("Agentes corriendo aca " + host.getSimulation().agentsRunning());
							Registry reg = LocateRegistry.getRegistry(host.getAgentsBalancer().getCoordinator().host(), host.getAgentsBalancer().getCoordinator().port());
							AgentsBalancer balancer = (AgentsBalancer)reg.lookup(Node.AGENTS_BALANCER);

							List<NodeAgent> nodeAgentsToMove = new ArrayList<NodeAgent>();
							for(Agent agent : host.getSimulation().getAgentsRunning()){
								NodeAgent nodeAgent = new NodeAgent(host.getNodeInformation(), agent);
								nodeAgentsToMove.add(nodeAgent);

							}

							balancer.shutdown(nodeAgentsToMove);
							for(NodeAgent nodeAgent : nodeAgentsToMove)
								host.getSimulation().remove(nodeAgent.agent());

									System.out.println("Agentes corriendo aca " + host.getSimulation().agentsRunning());
									//host.getCluster().disconnectFromGroup(host.getNodeInformation());
									//System.exit(0);
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
							//System.out.println("Agentes corriendo aca " + host.getSimulation().agentsRunning());

							Thread newStatistics = new CleanableThread("newStatistics") {
								@Override
								public void run() {
									while(true){
										try {
											Thread.sleep(6000);
											for(NodeInformation nodeConnected : host.getCluster().connectedNodes()){
												StatisticReports statisticReports = host.getStatisticsFor(nodeConnected);
												if(statisticReports!=null){
													NodeStatistics nodeStatistics = statisticReports.getNodeStatistics();
													System.out.println("Node: " + nodeConnected);
													System.out.println("Count Agents: " + nodeStatistics.getNumberOfAgents());
													for(AgentState agentState:nodeStatistics.getAgentState()){
														System.out.println("State: " +  agentState);
													}
												}
											}
										} catch (RemoteException e) {
											e.printStackTrace();
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}

								}

							};
							newStatistics.start();

							Thread shutdown = new CleanableThread("shutDown") {
								@Override
								public void run() {
									BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
									try{
										String s = br.readLine();
										switch(Integer.valueOf(s)){
										case 1:
											host.shutdown();
											break;
										}
									} catch (IOException e){
									} catch (NotBoundException e) {
										// TODO Auto-generated catch block
										//e.printStackTrace();
									} catch (NotCoordinatorException e) {
										// TODO Auto-generated catch block
										//e.printStackTrace();
									}

								}

							};
							shutdown.start();
							node.startAndWait(Duration.standardMinutes(10));
							break;
						}
					} 
					catch (Exception e) {
						//System.out.println(e.getMessage());
					}
				}
			} catch (RemoteException e1) {
				//e1.printStackTrace();
			} catch (NumberFormatException e) {
				//e.printStackTrace();
			} catch (AlreadyBoundException e) {
				//e.printStackTrace();
			}
		}
		//NODE MAIN
		else{
			if(args.length==5){
				try {
					//arg0: host to connect
					//arg1: port to connect
					//arg2: node id
					//arg3: local host
					//arg4: local port
					final Host host = new Host(args[3], Integer.valueOf(args[4]), args[2]);
					host.connect(args[0], Integer.valueOf(args[1]));

					Registry connectedRegistry = LocateRegistry.getRegistry(args[0], Integer.valueOf(args[1]));
					final AgentsBalancer agentsBalancerConnected = (AgentsBalancer) connectedRegistry.lookup(Node.AGENTS_BALANCER);

					try {
						//						System.out.println("Envio una eleccion");
						agentsBalancerConnected.bullyElection(host.getNodeInformation(), System.currentTimeMillis());
						/*Thread.sleep(5000);
						//System.out.println("Ya termine la eleccion! que paso??");
						if(host.getAgentsBalancer().getCoordinator()==null){
							agentsBalancerConnected.bullyCoordinator(host.getNodeInformation(), System.currentTimeMillis());
							host.setCoordinator(host.getNodeInformation());

						}*/
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					//Commands
					while(true){
						System.out.println("list - close - newevent - events");
						BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						try {
							String s = br.readLine();
							switch(Command.toCommand(s)){
							case list:
								System.out.println(host.getCluster().connectedNodes());
								break;
							case close:
								Set<NodeInformation> connectedNodes = host.getCluster().connectedNodes();
								for(NodeInformation node : connectedNodes){
									if(!node.equals(host.getNodeInformation())){
										Registry reg = LocateRegistry.getRegistry(node.host(), node.port());
										ClusterAdministration connectedCluster = (ClusterAdministration)reg.lookup(Node.CLUSTER_COMUNICATION);
										connectedCluster.disconnectFromGroup(host.getNodeInformation());
									}
								}
								System.exit(0);
								break;
							case newevent:
								Resource resource = new Resource("Alloy", "Steel");
								NodeAgent nodeAgent = new NodeAgent(host.getNodeInformation(), new Producer("steel mine" + 1, resource, Duration.standardDays(1), 5));
								EventInformation event = new EventInformation("New Agent", host.getNodeInformation().id(), nodeAgent.agent());
								event.setReceivedTime(System.currentTimeMillis());
								host.getRemoteEventDispatcher().publish(event);

								break;
							case coord:
								System.out.println("Coordinator is: " + host.getAgentsBalancer().getCoordinator());
								break;
							case events:
								//System.out.println("Events: " + myEventDispatcher.newEventsFor(myNodeInformation));
								break;
							}
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (AlreadyBoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public enum Command
	{
		list, close, newevent, events, coord, agents, shutdown, add, connect, addAgent, c, p, m, copper, steel, gold, s, n;

		public static Command toCommand(String str)
		{
			try {
				return valueOf(str);
			} 
			catch (Exception ex) {
				return close;
			}
		}   
	}

	public enum ResourceType{
		gold, copper, steel;

		public static ResourceType toResource(String str)
		{
			try {
				return valueOf(str);
			} 
			catch (Exception ex) {
				return gold; //default
			}
		}   

	}
}


