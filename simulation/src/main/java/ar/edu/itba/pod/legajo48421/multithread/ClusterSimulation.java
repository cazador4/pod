package ar.edu.itba.pod.legajo48421.multithread;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import ar.edu.itba.balance.api.AgentsBalancer;
import ar.edu.itba.balance.api.NodeAgent;
import ar.edu.itba.balance.api.NotCoordinatorException;
import ar.edu.itba.node.Node;
import ar.edu.itba.node.NodeInformation;
import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.agent.runner.Simulation;
import ar.edu.itba.pod.legajo48421.node.api.Host;
import ar.edu.itba.pod.multithread.AgentThread;
import ar.edu.itba.pod.multithread.EventDispatcher;
import ar.edu.itba.pod.multithread.LocalSimulation;
import ar.edu.itba.pod.time.TimeMapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ClusterSimulation extends LocalSimulation{

	//private final EventDispatcher dispatcher2;
	private final TimeMapper timeMapper;

	private Thread timeOutThread;
	private List<AgentThread> agents = Lists.newArrayList();
	private AtomicBoolean running = new AtomicBoolean();

	// expected start and end date of the simulation.
	private DateTime start;
	private DateTime end;

	private Host host;

	public ClusterSimulation(TimeMapper timeMapper, Host host){
		super(timeMapper);
		checkNotNull(timeMapper, "Time mapper cannot be null");
		checkNotNull(host, "Host cannot be null");
		this.timeMapper = timeMapper;
		this.host = host;
		//dispatcher2 = new ExtendedMultiThreadEventDispatcher(host);
	}

	public EventDispatcher dispatcher() {
		return host.getExtendedMultiThreadEventDispatcher();
	}
	
/*	@Override
	public void add(Agent agent) {
		checkNotNull(agent, "Agent cannot be null");
		checkArgument(!this.agents.contains(agent), "Cannot add the same agent twice");
		NodeInformation coordinator = host.getCoordinator();
		Registry registry;
		try {
			registry = LocateRegistry.getRegistry(coordinator.host(), coordinator.port());
			AgentsBalancer agentsBalancer = (AgentsBalancer)registry.lookup(Node.AGENTS_BALANCER);
			agentsBalancer.addAgentToCluster(new NodeAgent(host.getNodeInformation(), agent));
			AgentThread thread = new AgentThread(timeMapper, dispatcher, agent);
			addAgentThread(thread);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (NotCoordinatorException e) {
			e.printStackTrace();
		}
	}

	protected void addAgentThread(AgentThread thread) {
		this.agents.add(thread);

		if (running.get()) {
			thread.start();
		}
	}*/
}
