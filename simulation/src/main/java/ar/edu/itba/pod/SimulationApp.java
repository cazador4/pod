package ar.edu.itba.pod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;

import org.joda.time.Duration;

import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.NodeStatistics;
import ar.edu.itba.node.api.StatisticReports;
import ar.edu.itba.pod.agent.market.AgentState;
import ar.edu.itba.pod.agent.market.Consumer;
import ar.edu.itba.pod.agent.market.Market;
import ar.edu.itba.pod.agent.market.Producer;
import ar.edu.itba.pod.agent.market.Resource;
import ar.edu.itba.pod.legajo48421.node.api.Host;
import ar.edu.itba.pod.legajo48421.node.api.Main.Command;
import ar.edu.itba.pod.thread.CleanableThread;
import ar.edu.itba.pod.time.TimeMapper;
import ar.edu.itba.pod.time.TimeMappers;

public class SimulationApp implements Runnable {

	public static void main(String[] args) {
		new SimulationApp().run();
	}

	@Override
	public void run() {

		final TimeMapper timeMapper = TimeMappers.oneSecondEach(Duration.standardHours(6));
		//final Host host;
		try {
			//node.add(new Producer("steel mine 1", steel, Duration.standardDays(1), 5));
			//			node.add(new Producer("steel mine 1", steel, Duration.standardDays(1), 5));
			//			node.add(new Producer("copper mine1", copper, Duration.standardDays(1), 10));
			//						node.add(new Producer("gold mine 1", gold, Duration.standardDays(1), 1));
			//			node.add(new Consumer("copper consumer1", copper, Duration.standardHours(8), 2));
			//			node.add(new Consumer("steel consumer1", steel, Duration.standardDays(2), 4));
			//			node.add(new Consumer("steel consumer2", steel, Duration.standardDays(2), 4));
			//node.add(new Market("gold market", gold));
			//			node.add(new Market("cooper market", copper));
			//			node.add(new Market("steel market", steel));

			//		TransfersWatcher logger = new TransfersWatcher("logger", eventManager);
			//		node.addAgent(logger);
			System.out.println("Starting ...");

			Thread menuThread = new CleanableThread("Menu"){
				public void run(){
					boolean connect = false;
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
						System.out.println("Ingrese IP");
						String ipAddress;
						ipAddress = br.readLine();
						System.out.println("Puerto");
						String port = br.readLine();
						final Host host = new Host(ipAddress, Integer.valueOf(port), ipAddress+":"+port);
						//ClusterSimulation node = null;
						boolean firstTime = false;
						System.out.println("Primer nodo que se levanta? s - n");
						String firstNode = br.readLine();
						switch(Command.toCommand(firstNode)){
						case s:
							firstTime = true;
							break;
						case n:
							firstTime = false;
							break;
						}
						while(true){
							System.out.println("connect - list - agents - coord - addAgent - shutdown - events");
							try {
								String s = br.readLine();
								switch(Command.toCommand(s)){
								case connect:
									connect=true;
									if(!firstTime){
										System.out.println("Ingrese IP a conectar");
										String ipToConnect = br.readLine();
										System.out.println("Ingrese Puerto");
										String portToConnect = br.readLine();
										//node = new ClusterSimulation(timeMapper, host);
										System.out.println("Termina el cluster");
										//host.setSimulation(node);
										//Registry connectedRegistry = LocateRegistry.getRegistry(ipToConnect, Integer.valueOf(portToConnect));
										host.connect(ipToConnect, Integer.valueOf(portToConnect));
									}
									else
									{
										host.getAgentsBalancer().bullyElection(host.getNodeInformation(), System.currentTimeMillis());
										host.getCluster().createGroup();
										System.out.println("Grupo creado");

									}
									break;
								case list:
									System.out.println(host.getCluster().connectedNodes());
									System.out.println(host.getAgentsBalancer().getList());
									break;
								case agents:
									System.out.println("Agentes corriendo aca " + host.getSimulation().agentsRunning());
									break;
								case coord:
									System.out.println("Coordinator is: " + host.getAgentsBalancer().getCoordinator());
									break;
								case addAgent:
									if(connect){
										Resource resourceSelected=null;
										System.out.println("Ingrese resource");
										String resource = br.readLine();
										switch(Command.toCommand(resource)){
										case copper:
											resourceSelected = new Resource("Mineral", "Copper");
											break;
										case steel:
											resourceSelected = new Resource("Alloy", "Steel");
											break;
										case gold:
											resourceSelected = new Resource("Mineral", "Gold");
											break;
										}
										System.out.println("Ingrese el tipo de agente: c - p - m");
										String type = br.readLine();
										switch(Command.toCommand(type)){
										case c:
											host.getSimulation().add(new Consumer(resourceSelected.name() + " consumer " + host.getNodeInformation().port(), resourceSelected, Duration.standardDays(1), 5));
											break;
										case p:
											host.getSimulation().add(new Producer(resourceSelected.name() + " mine " + host.getNodeInformation().port(), resourceSelected, Duration.standardDays(1), 5));
											break;
										case m:
											host.getSimulation().add(new Market(resourceSelected.name() + " market " + host.getNodeInformation().port(), resourceSelected));
											break;
										}
									}
									break;
								case shutdown:
									host.shutdown();
									break;
								case events:
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
									host.getSimulation().startAndWait(Duration.standardMinutes(10));
									break;
								}
							} 
							catch (Exception e) {
								System.out.println(e.getMessage());
							}
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (NumberFormatException e) {
						e.printStackTrace();
					} catch (AlreadyBoundException e) {
						e.printStackTrace();
					}
				}
			};
			menuThread.start();


		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		}
		//System.out.println("Ending ...");
		//		System.out.println("Total transactions: " + Iterables.size(logger.transferItems()));
	}
}
