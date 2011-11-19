package ar.edu.itba.pod;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.joda.time.Duration;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.node.Node;
import ar.edu.itba.pod.agent.market.Consumer;
import ar.edu.itba.pod.agent.market.Market;
import ar.edu.itba.pod.agent.market.Producer;
import ar.edu.itba.pod.agent.market.Resource;
import ar.edu.itba.pod.legajo48421.multithread.ClusterSimulation;
import ar.edu.itba.pod.legajo48421.node.api.Host;
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

			try {
//				System.out.println("Envio una eleccion");
				agentsBalancerConnected.bullyElection(host.getNodeInformation(), System.currentTimeMillis());
				Thread.sleep(5000);
				//System.out.println("Ya termine la eleccion! que paso??");
				if(host.getCoordinator()==null){
					agentsBalancerConnected.bullyCoordinator(host.getNodeInformation(), System.currentTimeMillis());
					host.setCoordinator(host.getNodeInformation());
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}	
			
			
			ClusterSimulation node = new ClusterSimulation(timeMapper, host);

			Resource gold = new Resource("Mineral", "Gold");
			Resource copper = new Resource("Mineral", "Copper");
			Resource steel = new Resource("Alloy", "Steel");

			for (int i = 0; i < 1; i++) {
				node.add(new Producer("steel mine" + i, steel, Duration.standardDays(1), 5));
				//node.add(new Producer("copper mine" + i, copper, Duration.standardDays(1), 10));
				//node.add(new Producer("gold mine" + i, gold, Duration.standardDays(1), 1));
				//node.add(new Consumer("copper consumer" + i, copper, Duration.standardHours(8), 2));
				//node.add(new Consumer("gold consumer" + i, gold, Duration.standardDays(2), 4));
			}

//			node.add(new Market("gold market", gold));
//			node.add(new Market("cooper market", copper));
			//node.add(new Market("steel market", steel));

			//		TransfersWatcher logger = new TransfersWatcher("logger", eventManager);
			//		node.addAgent(logger);

			System.out.println("Starting ...");
			node.startAndWait(Duration.standardMinutes(10));
			
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (RemoteException e1) {
			e1.printStackTrace();
		} catch (AlreadyBoundException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		System.out.println("Ending ...");
		//		System.out.println("Total transactions: " + Iterables.size(logger.transferItems()));
	}
}
