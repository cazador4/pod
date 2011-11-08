package ar.edu.itba.pod;

import org.joda.time.Duration;

import ar.edu.itba.pod.agent.market.Consumer;
import ar.edu.itba.pod.agent.market.Market;
import ar.edu.itba.pod.agent.market.Producer;
import ar.edu.itba.pod.agent.market.Resource;
import ar.edu.itba.pod.multithread.LocalSimulation;
import ar.edu.itba.pod.time.TimeMapper;
import ar.edu.itba.pod.time.TimeMappers;

public class SimulationApp implements Runnable {

	public static void main(String[] args) {
		new SimulationApp().run();
	}

	@Override
	public void run() {

		TimeMapper timeMapper = TimeMappers.oneSecondEach(Duration.standardHours(6));
		LocalSimulation node = new LocalSimulation(timeMapper);

		Resource gold = new Resource("Mineral", "Gold");
		Resource copper = new Resource("Mineral", "Copper");
		Resource steel = new Resource("Alloy", "Steel");
		
		for (int i = 0; i < 1; i++) {
			node.add(new Producer("steel mine" + i, steel, Duration.standardDays(1), 5));
			node.add(new Producer("copper mine" + i, copper, Duration.standardDays(1), 10));
			node.add(new Producer("gold mine" + i, gold, Duration.standardDays(1), 1));

			
			node.add(new Consumer("steel consumer" + i, steel, Duration.standardDays(3), 2));
			node.add(new Consumer("copper consumer" + i, copper, Duration.standardHours(8), 2));
			node.add(new Consumer("gold consumer" + i, gold, Duration.standardDays(2), 4));
		}

		node.add(new Market("gold market", gold));
		node.add(new Market("cooper market", copper));
		node.add(new Market("steel market", steel));
		
//		TransfersWatcher logger = new TransfersWatcher("logger", eventManager);
//		node.addAgent(logger);

		System.out.println("Starting ...");
		try {
			node.startAndWait(Duration.standardMinutes(10));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("Ending ...");
//		System.out.println("Total transactions: " + Iterables.size(logger.transferItems()));
	}
}
