package ar.edu.itba.pod.legajo48421.node.api;

import ar.edu.itba.balance.api.NotCoordinatorException;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.NodeStatistics;
import ar.edu.itba.node.api.StatisticReports;
import ar.edu.itba.pod.agent.market.AgentState;
import ar.edu.itba.pod.agent.market.Market;
import ar.edu.itba.pod.agent.market.Producer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.joda.time.Duration;

import ar.edu.itba.pod.agent.market.Consumer;
import ar.edu.itba.pod.agent.market.Resource;
import ar.edu.itba.pod.legajo48421.node.api.Main.Command;
import ar.edu.itba.pod.thread.CleanableThread;

public class Main2 {
	public static void main(String[] args) {
		try {
			final Host host = new Host("127.0.0.1", 1239, "127.0.0.1:1239");
			//host.connect("127.0.0.1", 1234);
			host.getAgentsBalancer().bullyElection(host.getNodeInformation(), System.currentTimeMillis());
			host.getCluster().createGroup();
			Resource resourceSelected = new Resource("Mineral", "Gold");
			
			host.getSimulation().add(new Producer(resourceSelected.name() + " producer222 " + host.getNodeInformation().port(), resourceSelected, Duration.standardDays(1), 5));
//			host.getSimulation().add(new Consumer(resourceSelected.name() + " consumer222 " + host.getNodeInformation().port(), resourceSelected, Duration.standardDays(1), 5));
//			host.getSimulation().add(new Consumer(resourceSelected.name() + " consumer " + host.getNodeInformation().port(), resourceSelected, Duration.standardDays(1), 5));
//			host.getSimulation().add(new Consumer(resourceSelected.name() + " consumer " + host.getNodeInformation().port(), resourceSelected, Duration.standardDays(1), 5));
//			host.getSimulation().add(new Consumer(resourceSelected.name() + " consumer " + host.getNodeInformation().port(), resourceSelected, Duration.standardDays(1), 5));
			host.getSimulation().add(new Consumer(resourceSelected.name() + " consumer222 " + host.getNodeInformation().port(), resourceSelected, Duration.standardDays(1), 5));
			host.getSimulation().add(new Market(resourceSelected.name() + " market222 "	 + host.getNodeInformation().port(), resourceSelected));
			Thread shutdown = new CleanableThread("threadShutdown") {
				@Override
				public void run() {
					try {
						while(true){
							BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
							String shut = br.readLine();
							if(Command.toCommand(shut).equals(Command.shutdown)){
								host.shutdown();
							} 
						}
					}catch (RemoteException e) {
						e.printStackTrace();
					} catch (NotBoundException e) {
						e.printStackTrace();
					} catch (NotCoordinatorException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			shutdown.start();
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
			
		} catch (RemoteException e) {
			e.printStackTrace();
		//} catch (NotBoundException e) {
		//	e.printStackTrace();
		} catch (AlreadyBoundException e) {
			e.printStackTrace();
		//} catch (NotCoordinatorException e) {
			//e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
