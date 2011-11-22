package ar.edu.itba.pod.legajo48421.balance.api;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.AgentsTransfer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.balance.api.NotCoordinatorException;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.pod.legajo48421.node.api.Constant;
import ar.edu.itba.pod.legajo48421.node.api.Host;
import ar.edu.itba.pod.thread.CleanableThread;

public class AgentsBalancerImpl implements AgentsBalancer{

	private Host host;
	private NodeInformation myNode;
	private AtomicBoolean isOnElection = new AtomicBoolean(false);
	private Map<NodeInformation, Long> elections; //msg elections
	private Map<NodeInformation, Long> coordinators; //msg coordinators
	private AtomicBoolean isOk = new AtomicBoolean(true);
	private List<NodeCountAgents> nodeCountAgentsList;
	private volatile NodeInformation coord;
	CountDownLatch coordinatorLock = new CountDownLatch(1);

	public AgentsBalancerImpl(Host host){
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		this.host = host;
		myNode = host.getNodeInformation();
		elections = new ConcurrentHashMap<NodeInformation, Long>();
		coordinators = new ConcurrentHashMap<NodeInformation, Long>();
		nodeCountAgentsList = new ArrayList<NodeCountAgents>();

		Thread cleanOldElections = new CleanableThread("cleanOldElections") {
			@Override
			public void run() {
				try {
					while(true){
						Thread.sleep(Constant.CLEAR_LISTS);
						if(!isOnElection.get())
							elections.clear();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		cleanOldElections.start();
	}

	public List<NodeCountAgents> getList(){
		return nodeCountAgentsList;
	}

	@Override
	public void bullyElection(final NodeInformation node, final long timestamp)
			throws RemoteException {
		if(checkElection(node, timestamp)){
			//VEO SI TENGO MAYOR ID QUE EL QUE LLEGA
			coord=null;
			if(myNode.id().compareTo(node.id())>0){
				isOk.set(true);
				//System.out.println("Hice un ok! al nodo: "+ node);
				if(!isOnElection.getAndSet(true)){
					Thread newElection = new CleanableThread("newElection"){
						public void run(){
							try {
								host.getAgentsBalancerFor(node).bullyOk(myNode);
								//System.out.println("Mande una eleccion!");
								long timeElection = System.currentTimeMillis();
								for(final NodeInformation connectedNode : host.getCluster().connectedNodes()){
									if(!connectedNode.equals(myNode) && !connectedNode.equals(node)){
										host.getAgentsBalancerFor(connectedNode).bullyElection(myNode, timeElection);
									}
								}
								try {
									Thread.sleep(Constant.WAIT_FOR_COORDINATOR);
									isOnElection.set(false);
									if(isOk.get()){
										//System.out.println("Seteo que el coord soy yo!");
										long timeCoord=System.currentTimeMillis();
										for(NodeInformation connectedNode2 : host.getCluster().connectedNodes()){
											host.getAgentsBalancerFor(connectedNode2).bullyCoordinator(myNode, timeCoord);
										}
										//host.setCoordinator(myNode);
										coord=myNode;
										reloadAndBalanceNodeCountAgentsList();
									}
								} catch (InterruptedException e) {
									//e.printStackTrace();
								}
							} catch (RemoteException e) {
							}
						}
					};
					newElection.start();
				}
				else{
					host.getAgentsBalancerFor(node).bullyOk(myNode);
				}
			}
			else{
				if(host.getCluster().connectedNodes().size()==1){
					long timeCoord=System.currentTimeMillis();
					bullyCoordinator(myNode, timeCoord);
					reloadAndBalanceNodeCountAgentsList();
				}
				else{
					//System.out.println("Mi nodo es menor");
					Thread broadcastElection = new CleanableThread("broadcastElection") {
						public void run(){
							try {
								//System.out.println("Hago broadCast de la eleccion del nodo: "+ node + " con tiempo "+ timestamp);
								for(NodeInformation connectedNode : host.getCluster().connectedNodes()){
									if(!connectedNode.equals(node) && !connectedNode.equals(myNode)){
										try{
											host.getAgentsBalancerFor(connectedNode).bullyElection(node, timestamp);
										} catch(RemoteException e1){
											//e1.printStackTrace();
										}
									}
								}
							} catch (RemoteException e) {
								//e.printStackTrace();
							}
						}
					};
					broadcastElection.start();
				}
			}
		}
	}

	@Override
	public void bullyOk(NodeInformation node) throws RemoteException {
		isOk.set(false);
		//System.out.println("El nodo " + node + " Me hicieron un ok");
	}

	public AtomicBoolean getIsOk(){
		return isOk;
	}

	private synchronized boolean checkElection(NodeInformation node, long timestamp){
		if(!elections.containsKey(node) || elections.get(node)<timestamp)
		{
			elections.put(node, timestamp);
			return true;
		}
		return false;
	}

	public synchronized boolean checkCoordinator(NodeInformation node, long timestamp){
		if(!coordinators.containsKey(node) || coordinators.get(node)<timestamp){
			coordinators.put(node, timestamp);
			return true;
		}
		return false;
	}

	@Override
	public void bullyCoordinator(final NodeInformation node, final long timestamp)
			throws RemoteException {
		if(checkCoordinator(node, timestamp)){
			coord=node;
			coordinatorLock.countDown(); 
			isOnElection.set(false);
//			System.out.println("El coordinador es: " + node +" en el tiempo: " + timestamp);
			Thread newCoordinator = new CleanableThread("newCoordinator") {
				public void run(){
					try {
						for(NodeInformation connectedNode : host.getCluster().connectedNodes()){
							if(!connectedNode.equals(node) && !connectedNode.equals(myNode) ){
								try{
									host.getAgentsBalancerFor(connectedNode).bullyCoordinator(node, timestamp);
								} catch (RemoteException e) {
									e.printStackTrace();
								}
							}
						}
					} catch (RemoteException e) {
						//e.printStackTrace();
					}
				}
			};
			newCoordinator.start();
		}
	}

	@Override
	public void shutdown(List<NodeAgent> agents) throws RemoteException,
	NotCoordinatorException {
		if(getCoordinator().equals(myNode)){
			NodeAgent nodeAgent = agents.get(0);
			NodeInformation nodeToShutdown = nodeAgent.node();
			nodeCountAgentsList.remove(new NodeCountAgents(nodeToShutdown, agents.size()));
			NodeInformation nodeToAdd = nodeCountAgentsList.get(0).nodeInformation;
			nodeCountAgentsList.get(0).setCountAgents(agents.size()+ nodeCountAgentsList.get(0).countAgents);
			host.getAgentsTransferFor(nodeToAdd);
			AgentsTransfer agentsTransfer = host.getAgentsTransferFor(nodeToAdd);
			agentsTransfer.runAgentsOnNode(agents);
			try {
				host.getCluster().disconnectFromGroup(nodeToShutdown);
				host.getAgentsBalancer().getCoordinator();
				reloadAndBalanceNodeCountAgentsList();
			} catch (NotBoundException e) {
				//e.printStackTrace();
			}
		}
		else
			throw new NotCoordinatorException(host.getAgentsBalancer().getCoordinator());
	}

	@Override
	public void addAgentToCluster(NodeAgent agent) throws RemoteException,
	NotCoordinatorException {
		if(getCoordinator().equals(myNode)){
			Collections.sort(nodeCountAgentsList);
			if(nodeCountAgentsList.isEmpty()){
				List<NodeAgent> result = new ArrayList<NodeAgent>();
				result.add(agent);
				host.getAgentsTransfer().runAgentsOnNode(result);
			}else{
				Collections.sort(nodeCountAgentsList);
				NodeCountAgents nodeCountAgent = nodeCountAgentsList.get(0);
				AgentsTransfer agentsTransfer = host.getAgentsTransferFor(nodeCountAgent.getNodeInformation()); 
				List<NodeAgent> result = new ArrayList<NodeAgent>();
				result.add(agent);
				agentsTransfer.runAgentsOnNode(result);
				nodeCountAgent.addAgent();
				Collections.sort(nodeCountAgentsList);
			}
		}
		else
			throw new NotCoordinatorException(host.getNodeInformation());
	}

	public NodeInformation getCoordinator(){
		NodeInformation nodeToReturn=null;
		if (coord != null) {
			nodeToReturn = coord;
		} else {
			while(isOnElection.get()) {
				try {
					isOnElection.set(coordinatorLock.await(5000, TimeUnit.MILLISECONDS));
					if (isOnElection.get()) {
						bullyElection(myNode, DateTime.now().getMillis());
					} else {
						coordinatorLock = new CountDownLatch(1);
						nodeToReturn = coord;
					}
				} catch (InterruptedException e) {
				} catch (RemoteException e) {
				}
			}
		}
		return nodeToReturn;
	}

	//balance
	
	private Thread reloadThread;

	public void reloadAndBalanceNodeCountAgentsList(){
		reloadThread = new CleanableThread("reloadThread") {
			public void run(){
				System.out.println("starting balance agents...");
				nodeCountAgentsList.clear();
				try {
					for(NodeInformation connectedNode : host.getCluster().connectedNodes()){
						AgentsTransfer agentsTransfer = host.getAgentsTransferFor(connectedNode);
						NodeCountAgents nodeCountAgents = new NodeCountAgents(connectedNode, agentsTransfer.getNumberOfAgents());
						nodeCountAgentsList.add(nodeCountAgents);
					}
					balance(nodeCountAgentsList);
					balanceInNodes();
				} catch (AccessException e) {
					//e.printStackTrace();
				} catch (RemoteException e) {
					//e.printStackTrace();
				}
			}

			private void balanceInNodes() throws RemoteException {
				List<NodeAgent> nodesToAdd = new ArrayList<NodeAgent>();
				for(NodeCountAgents nodeCountAgent : nodeCountAgentsList){
					AgentsTransfer agentsTransfer = host.getAgentsTransferFor(nodeCountAgent.getNodeInformation());
					int diff = agentsTransfer.getNumberOfAgents()-nodeCountAgent.countAgents;
					if(diff > 0){
						nodesToAdd.addAll(agentsTransfer.stopAndGet(diff));
					}
				}
				//Add agents to every node
				for(NodeCountAgents nodeCountAgent : nodeCountAgentsList){
					AgentsTransfer agentsTransfer = host.getAgentsTransferFor(nodeCountAgent.getNodeInformation());
					int diff = agentsTransfer.getNumberOfAgents()-nodeCountAgent.countAgents;
					if(diff < 0){
						List<NodeAgent> result = new ArrayList<NodeAgent>();
						for(int i=0; i<(diff*-1); i++)
							result.add(nodesToAdd.get(i));
						agentsTransfer.runAgentsOnNode(result);
					}
				}				
			}

			public void balance(List<NodeCountAgents> nodeCountAgentsList){
				Comparator<NodeCountAgents> comparator = Collections.reverseOrder();
				Collections.sort(nodeCountAgentsList, comparator);

				boolean condition=false;
				int cantFirst = nodeCountAgentsList.get(0).getCountAgents();
				int cantLast = nodeCountAgentsList.get(nodeCountAgentsList.size()-1).getCountAgents();
				int diff = cantFirst-cantLast;
				if(diff>1){
					condition=true;
				}
				while(condition){
					int newFirst = cantFirst-diff/2;
					int newLast = cantLast+diff/2;
					nodeCountAgentsList.get(0).setCountAgents(newFirst);
					nodeCountAgentsList.get(nodeCountAgentsList.size()-1).setCountAgents(newLast);
					Collections.sort(nodeCountAgentsList, comparator);
					cantFirst = nodeCountAgentsList.get(0).getCountAgents();
					cantLast = nodeCountAgentsList.get(nodeCountAgentsList.size()-1).getCountAgents();
					diff = cantFirst-cantLast;
					if(diff>1)
						condition=true;
					else
						condition=false;
				}
			}
		};
		reloadThread.start();
	}

	private class NodeCountAgents implements Comparable<NodeCountAgents>{
		private int countAgents;
		private NodeInformation nodeInformation;

		public NodeCountAgents(NodeInformation nodeInformation, int countAgent){
			this.nodeInformation = nodeInformation;
			this.countAgents = countAgent;
		}

		public NodeInformation getNodeInformation(){
			return nodeInformation;
		}

		public int getCountAgents(){
			return countAgents;
		}

		public void setCountAgents(int countAgents){
			this.countAgents = countAgents;
		}

		public void addAgent(){
			countAgents++;
		}

		@Override
		public int compareTo(NodeCountAgents arg0) {
			if(arg0!=null){
				if(countAgents>arg0.countAgents)
					return 1;
				else{
					if(countAgents==arg0.countAgents)
						return 0;
					else
						return -1;
				}		
			}
			return 0;
		}

		public String toString(){
			return nodeInformation.toString() + " " + countAgents;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + countAgents;
			result = prime
					* result
					+ ((nodeInformation == null) ? 0 : nodeInformation
							.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NodeCountAgents other = (NodeCountAgents) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (countAgents != other.countAgents)
				return false;
			if (nodeInformation == null) {
				if (other.nodeInformation != null)
					return false;
			} else if (!nodeInformation.equals(other.nodeInformation))
				return false;
			return true;
		}

		private AgentsBalancerImpl getOuterType() {
			return AgentsBalancerImpl.this;
		}


	}

}
