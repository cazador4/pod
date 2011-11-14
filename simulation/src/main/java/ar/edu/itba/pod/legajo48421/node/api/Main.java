package ar.edu.itba.pod.legajo48421.node.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Set;

import org.joda.time.Duration;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.event.EventInformation;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.node.api.ClusterAdministration;
import ar.edu.itba.pod.agent.market.Producer;
import ar.edu.itba.pod.agent.market.Resource;
import ar.edu.itba.pod.thread.CleanableThread;

public class Main {
	//Main Server
	public static void main(String[] args){
		//CLUSTER MAIN
		if(args.length==3){
			//CADA NODO TIENE QUE SABER QUIEN ES EL NODO COORDINADOR.
			try {
				Host host = new Host(args[0], Integer.valueOf(args[1]), args[2]);
				host.getCluster().createGroup();
				
				//host.getAgentsBalancer().bullyCoordinator(host.getNodeInformation(), System.currentTimeMillis());

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
						case close:
							host.getCluster().disconnectFromGroup(host.getNodeInformation());
							System.exit(0);
							break;
						case newevent:
							Resource resource = new Resource("Alloy", "Steel");
							NodeAgent nodeAgent = new NodeAgent(host.getNodeInformation(), new Producer("steel mine" + 1, resource, Duration.standardDays(1), 5));
							host.getRemoteEventDispatcher().publish(new EventInformation("New Agent", host.getNodeInformation().id() + System.currentTimeMillis(), nodeAgent.agent()));
							break;
						case coord:
							System.out.println("Coordinator is: " + host.getCoordinator());
							break;
						case events:
							break;
						}
					} 
					catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			} catch (RemoteException e1) {
				e1.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (AlreadyBoundException e) {
				e.printStackTrace();
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
					
					Thread newElection = new CleanableThread("newElection"){
						public void run(){
							try {
								agentsBalancerConnected.bullyElection(host.getNodeInformation(), System.currentTimeMillis());
								Thread.sleep(Constant.WAIT_FOR_COORDINATOR);
								if(host.getCoordinator()==null){
									agentsBalancerConnected.bullyCoordinator(host.getNodeInformation(), System.currentTimeMillis());
									host.setCoordinator(host.getNodeInformation());
								}
							} catch (RemoteException e) {
								e.printStackTrace();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					};
					newElection.start();
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
								System.out.println("Coordinator is: " + host.getCoordinator());
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
		list, close, newevent, events, coord;

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


