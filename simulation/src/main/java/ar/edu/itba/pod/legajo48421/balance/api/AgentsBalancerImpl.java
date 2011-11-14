package ar.edu.itba.pod.legajo48421.balance.api;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.balance.api.NotCoordinatorException;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.pod.legajo48421.node.api.Constant;
import ar.edu.itba.pod.legajo48421.node.api.Host;
import ar.edu.itba.pod.thread.CleanableThread;

public class AgentsBalancerImpl implements AgentsBalancer{

	private Host host;
	private long timestampElection;
	private NodeInformation myNode;
	private AtomicBoolean havePossibility = new AtomicBoolean(true);
	private long timestampCoordinator;

	public AgentsBalancerImpl(Host host){
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		this.host = host;
		myNode = host.getNodeInformation();
	}

	@Override
	public void bullyElection(NodeInformation node, long timestamp)
			throws RemoteException {
		if(timestamp!=timestampElection){
			final NodeInformation sendElectionNode = node;
			final long sendTimestamp = timestamp;
			havePossibility.set(true);
			timestampElection = timestamp;
			//VEO SI TENGO MAYOR ID QUE EL QUE LLEGA
			if(myNode.id().compareTo(node.id())>0){
				getBalancer(node).bullyOk(myNode);
				for(final NodeInformation connectedNode : host.getCluster().connectedNodes()){
					if(connectedNode!=myNode && connectedNode!=node){
						Thread newElection = new CleanableThread("newElection"){
							public void run(){
								try {
									getBalancer(connectedNode).bullyElection(myNode, System.currentTimeMillis());
									try {
										Thread.sleep(Constant.WAIT_FOR_COORDINATOR);
										if(havePossibility.get()){
											for(NodeInformation connectedNode : host.getCluster().connectedNodes()){
												if(connectedNode!=myNode){
													getBalancer(connectedNode).bullyCoordinator(myNode, System.currentTimeMillis());
												}
											}
											host.setCoordinator(myNode);
										}
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								} catch (RemoteException e) {
									try {
										host.getCluster().disconnectFromGroup(connectedNode);
									} catch (RemoteException e1) {
										e1.printStackTrace();
									} catch (NotBoundException e1) {
										e1.printStackTrace();
									}
								}
							}
						};
						newElection.start();
						
					}
				}
			}
			else{
				for(final NodeInformation connectedNode : host.getCluster().connectedNodes()){
					if(connectedNode!=myNode && connectedNode!=node){
						Thread newElection = new CleanableThread("newElection"){
							public void run(){
								try {
									getBalancer(connectedNode).bullyElection(sendElectionNode, sendTimestamp);
								} catch (RemoteException e) {
									try {
										host.getCluster().disconnectFromGroup(connectedNode);
									} catch (RemoteException e1) {
										e1.printStackTrace();
									} catch (NotBoundException e1) {
										e1.printStackTrace();
									}
								}
							}
						};
						newElection.start();
						
					}
				}
			}
		}
	}

	private AgentsBalancer getBalancer(NodeInformation node){
		 try {
			 Registry reg = LocateRegistry.getRegistry(node.host(), node.port());
			return (AgentsBalancer)reg.lookup(Node.AGENTS_BALANCER);
		} catch (AccessException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			try {
				host.getCluster().disconnectFromGroup(node);
			} catch (RemoteException e1) {
				e1.printStackTrace();
			} catch (NotBoundException e1) {
				e1.printStackTrace();
			}
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void bullyOk(NodeInformation node) throws RemoteException {
		havePossibility.set(false);
	}

	@Override
	public void bullyCoordinator(NodeInformation node, long timestamp)
			throws RemoteException {
		if(timestamp!=timestampCoordinator){
			timestampCoordinator=timestamp;
			host.setCoordinator(node);
			for(NodeInformation connectedNode : host.getCluster().connectedNodes()){
				if(!connectedNode.equals(node) && !connectedNode.equals(myNode) ){
					getBalancer(connectedNode).bullyCoordinator(node, timestamp);
				}
			}
		}
	}

	@Override
	public void shutdown(List<NodeAgent> agents) throws RemoteException,
	NotCoordinatorException {
	}

	@Override
	public void addAgentToCluster(NodeAgent agent) throws RemoteException,
	NotCoordinatorException {
	}

}
