package ar.edu.itba.pod.multithread;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joda.time.DateTime;
import org.joda.time.Duration;

import ar.edu.itba.pod.agent.runner.Agent;
import ar.edu.itba.pod.agent.runner.Simulation;
import ar.edu.itba.pod.time.TimeMapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Multithreaded implementation of the simultion engine
 * <p> This implementation launches one thread per agent and run all of them concurrently </p>
 * <p>
 * For the implementation, it uses two threads:
 * <ul>
 *  <li> A "timeout" thread, that stops the simulation after a given timeout </li>
 *  <li> A "event-dispatcher" thread that processes the event queue </li>
 * </ul>
 *  
 * </p>
 */
public class LocalSimulation implements Simulation {
	private final EventDispatcher dispatcher = new MultiThreadEventDispatcher();
	private final TimeMapper timeMapper;

	private Thread timeOutThread;
	private List<AgentThread> agents = Lists.newArrayList();
	private AtomicBoolean running = new AtomicBoolean();

	// expected start and end date of the simulation.
	private DateTime start;
	private DateTime end;
	
	public LocalSimulation(TimeMapper timeMapper) {
		checkNotNull(timeMapper, "Time mapper cannot be null");
		this.timeMapper = timeMapper;
	}
	
	public EventDispatcher dispatcher() {
		return this.dispatcher;
	}

	@Override
	public void add(Agent agent) {
		checkNotNull(agent, "Agent cannot be null");
		checkArgument(!this.agents.contains(agent), "Cannot add the same agent twice");
		AgentThread thread = new AgentThread(timeMapper, dispatcher(), agent);
		addAgentThread(thread);
	}

	protected void addAgentThread(AgentThread thread) {
		this.agents.add(thread);
		
		if (running.get()) {
			thread.start();
		}
	}

	@Override
	public void remove(Agent agent) {
		checkNotNull(agent, "Agent cannot be null");
		for (AgentThread thread : agents) {
			if (thread.isRunning(agent)) {
				this.agents.remove(thread);
				if (running.get()) {
					thread.finishAgent();
				}
				return;
			}
		}
	}
	
	@Override
	public void start(final Duration duration) {
		Preconditions.checkState(running.compareAndSet(false, true), "The simulation is already running!");
		this.start = DateTime.now();
		this.end = duration.equals(Duration.ZERO) ? null : this.start.plus(duration);

		for (AgentThread agent : agents) {
			agent.start();
		}
		
		this.timeOutThread = new Thread("Simulation-Timeout") {
			@Override
			public void run() {
				try {
					sleep(duration.getMillis());
				} catch (InterruptedException e) {
					// this shouldn't happen
				}
				try {
					LocalSimulation.this.stop();
				} catch (InterruptedException e) {
					// this shouldn't happen
				}
			}
		};
		timeOutThread.setDaemon(true);
		timeOutThread.start();
	}

	@Override
	public void startAndWait(Duration duration) throws InterruptedException {
		Preconditions.checkArgument(duration.isLongerThan(Duration.ZERO), "Duration can't be 0");
		start(duration);
		timeOutThread.join();
	}
	
	@Override
	public void stop() throws InterruptedException {
		for (AgentThread agent : this.agents) {
			agent.finishAgent();
		}

		for (AgentThread agent : this.agents) {
			agent.join();
		}
	}
	
	@Override
	public Duration elapsed() {
		return new Duration(start, null);
	}
	
	public boolean started() {
		return start != null;
	}
	@Override
	public Duration remaining() {
		return new Duration(null, end);
	}
	
	@Override
	public int agentsRunning() {
		return agents.size();
	}

	protected TimeMapper getTimeMapper() {
		return timeMapper;
	}
	
	@Override
	public List<Agent> getAgentsRunning() {
		List<Agent> answer = Lists.newArrayList();
		for (AgentThread agentThread : agents) {
			answer.add(agentThread.getAgent());
		}
		return answer;
	}
}
