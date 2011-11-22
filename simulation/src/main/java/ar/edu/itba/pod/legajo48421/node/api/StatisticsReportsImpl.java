package ar.edu.itba.pod.legajo48421.node.api;

import ar.edu.itba.pod.agent.market.AgentState;
import ar.edu.itba.pod.agent.runner.Agent;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import ar.edu.itba.node.api.NodeStatistics;
import ar.edu.itba.node.api.StatisticReports;

public class StatisticsReportsImpl implements StatisticReports {

	private Host host;
	
	public StatisticsReportsImpl(Host host){
		try {
			UnicastRemoteObject.exportObject(this, 0);
			this.host = host;
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public NodeStatistics getNodeStatistics() throws RemoteException {
		List<AgentState> agentStates = new ArrayList<AgentState>();
		for(Agent agent : host.getSimulation().getAgentsRunning()){
			agentStates.add(agent.state());
		}
		return new NodeStatistics(host.getSimulation().getAgentsRunning().size(), agentStates);
	}

}
