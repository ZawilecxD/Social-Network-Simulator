package dataBaseAnalyzer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeIntervalStatisticsManager {

	private AtomicInteger runningThreads = new AtomicInteger(0);
	private ExecutorService cachedPool = Executors.newCachedThreadPool();
	
	private String selectCommentsForPostsForGivenTimeInterval = 
			"select c.author_id, p.author_id, SUM(c.sentiment) as sum1, SUM(c.sentiment2) as sum2"
			+" from comments c join posts p on c.post_id = p.id "
			+" where c.date BETWEEN ? AND ? "
			+" group by c.author_id,p.author_id;";
	
	public TimeIntervalStatisticsManager() {
		
	}
}
