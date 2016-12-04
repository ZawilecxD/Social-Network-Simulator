package dataBaseAnalyzer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeIntervalStatisticsManager {

	private AtomicInteger runningThreads = new AtomicInteger(0);
	private ExecutorService cachedPool = Executors.newCachedThreadPool();
	
	
	public TimeIntervalStatisticsManager() {
		
	}
}
