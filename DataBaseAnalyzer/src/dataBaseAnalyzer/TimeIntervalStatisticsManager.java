package dataBaseAnalyzer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeIntervalStatisticsManager {

	private AtomicInteger runningThreads = new AtomicInteger(0);
	private ExecutorService cachedPool = Executors.newCachedThreadPool();
	
//	private String selectCommentsForPostsForGivenTimeInterval = 
//			"select c.author_id, p.author_id, SUM(c.sentiment) as sum1, SUM(c.sentiment2) as sum2"
//			+" from comments c join posts p on c.post_id = p.id "
//			+" where c.date BETWEEN ? AND ? "
//			+" group by c.author_id,p.author_id;";
	
	private String selectUsersInteractionsForGivenTimeInterval = 
			"select * from users_interactions where date between ? and ?;";
	
	public TimeIntervalStatisticsManager() {
		
	}
	
	public void getUsersInteractionsForTimeInterval(Date from, Date to) {
		PreparedStatement ps = null;
		Connection c = DatabaseAnalyzerContext.databaseConnection();
		try {
			ps = c.prepareStatement(selectUsersInteractionsForGivenTimeInterval);
			ps.setDate(1, from);
			ps.setDate(2, to);
			User u1 = null, u2 = null;
			int fromId, toId, rowCount = 0;
			double sentiment, sentiment2;
			System.out.println("START: "+ps.toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				rowCount++;
				fromId = rs.getInt("source");
				toId = rs.getInt("target");
				sentiment = rs.getDouble("weight");
				sentiment2 = rs.getDouble("weight2");
				u1 = DatabaseAnalyzerContext.getUsers().get(fromId);
				u2 = DatabaseAnalyzerContext.getUsers().get(toId);
				if(DatabaseAnalyzerContext.USED_SENTIMENT == 2) {
					DatabaseAnalyzerContext.addEdge(u1, u2, sentiment2);
				} else {
					DatabaseAnalyzerContext.addEdge(u1, u2, sentiment);
				}
			}
			System.out.println("Fetched: "+rowCount+" users interactions");
		} catch(Exception e) {
			System.out.println("ERROR while processing data: "+from.toString()+" "+to.toString());
			e.printStackTrace();
		}
		
	}
}
