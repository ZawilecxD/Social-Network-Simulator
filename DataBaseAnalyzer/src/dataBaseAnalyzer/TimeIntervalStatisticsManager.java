package dataBaseAnalyzer;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
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
		String csvFile = CsvEditor.buildFilePath(from, to);
		Connection c = DatabaseAnalyzerContext.databaseConnection();
		try {
			System.out.println("Creating file: "+csvFile);
			FileWriter writer = new FileWriter(csvFile);
			ps = c.prepareStatement(selectUsersInteractionsForGivenTimeInterval);
			ps.setDate(1, from);
			ps.setDate(2, to);
			User u1 = null, u2 = null;
			int fromId, toId, rowCount = 0;
			double sentiment, sentiment2;
			String type = null;
			System.out.println("START: "+ps.toString());
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				rowCount++;
				fromId = rs.getInt("source");
				toId = rs.getInt("target");
				sentiment = rs.getDouble("weight");
				sentiment2 = rs.getDouble("weight2");
				type = rs.getString("type");
//				u1 = DatabaseAnalyzerContext.getUsers().get(fromId);
//				u2 = DatabaseAnalyzerContext.getUsers().get(toId);
//				if(DatabaseAnalyzerContext.USED_SENTIMENT == 2) {
//					DatabaseAnalyzerContext.addEdge(u1, u2, sentiment2);
//				} else {
//					DatabaseAnalyzerContext.addEdge(u1, u2, sentiment);
//				}
				CsvEditor.writeLine(writer, Arrays.asList(
						String.valueOf(fromId),
						String.valueOf(toId),
						String.valueOf(sentiment),
						String.valueOf(sentiment2),
						type
						)
				);
			}
			writer.flush();
	        writer.close();
			System.out.println("Fetched: "+rowCount+" users interactions");
		} catch(Exception e) {
			System.out.println("ERROR while processing data: "+from.toString()+" "+to.toString());
			e.printStackTrace();
		} finally {
			try {
				
				ps.close();
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
	}
}
