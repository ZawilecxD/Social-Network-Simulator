package dataBaseAnalyzer;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

/**
 * Creates information about users interactions in network and inserts it into users_interactions
 * @author Murzynas
 *
 */
public class UsersInteractionsManager {
	private AtomicInteger runningThreads = new AtomicInteger(0);
	private ExecutorService cachedPool = Executors.newCachedThreadPool();
	private AtomicInteger indexStart = new AtomicInteger(0);
	private int singleSelectSize = 5000;
	private int singleInsertBatchSize = 10000;

	private final String selectCommentsForPostsForGivenIds = 
			"select c.author_id, p.author_id, SUM(c.sentiment) as sum1, SUM(c.sentiment2) as sum2, c.date"
					+" from comments c join posts p on c.post_id = p.id "
					+" where c.author_id BETWEEN ? AND ? "
					+" group by c.author_id, p.author_id, c.date;";

	private final String selectCommentsForCommentsForGivenIds = 
			"select c.author_id, c2.author_id, SUM(c.sentiment) as sum1, SUM(c.sentiment2) as sum2, c.date "
					+"from comments c join comments c2 on c.parentcomment_id = c2.id " 
					+" where c.author_id BETWEEN ? AND ? "
					+"group by c.author_id, c2.author_id, c.date;";



	public UsersInteractionsManager(int singleSelectSize, int singleInsertBatchSize) {
		this.singleSelectSize = singleSelectSize;
		this.singleInsertBatchSize = singleInsertBatchSize;
	}

	public void collectInfoAndSaveInDatabase() {
		Instant startTime = Instant.now();
		User u = null;
		int start = 0, end = 0;
		int usersNumber = DatabaseAnalyzerContext.getUsers().keySet().size();
		while(start < usersNumber) {
			if(runningThreads.get() == DatabaseAnalyzerContext.MAX_THREADS_NUMBER) {
				continue;
			}
			start = indexStart.get();
			end = indexStart.addAndGet(singleSelectSize);
			if(start > usersNumber) {
				break;
			}
			cachedPool.execute(createRunnableCollectingData(DatabaseAnalyzerContext.databaseConnection(), start, end));
		};

		try {
			cachedPool.shutdown();
			cachedPool.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		System.out.println("POOL SHUTDOWN!!!");
	}

	private void saveCommentedPostsInfo(Connection c, ConcurrentLinkedQueue<String> queue, int start, int end) {
		PreparedStatement ps = null;
		int commentAuthorId = 0, postAuthorId = 0;
		double sentiment1 = 0, sentiment2 = 0;
		LocalDateTime date = null;
		try {
			ps = c.prepareStatement(selectCommentsForPostsForGivenIds);
			ps.setInt(1, start);
			ps.setInt(2, end);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				commentAuthorId = rs.getInt(1);
				postAuthorId = rs.getInt(2);
				sentiment1 = rs.getDouble("sum1");
				sentiment2 = rs.getDouble("sum2"); 
				date = LocalDateTime.ofInstant(rs.getTimestamp("date").toInstant(), ZoneId.systemDefault());
				queue.add(createInsert(commentAuthorId, postAuthorId, sentiment1, sentiment2, date));				
			}	
		} catch(Exception e) {
			System.out.println("ERROR while processing data: "+commentAuthorId+" "+postAuthorId+" "+sentiment1+" "+sentiment2+" "+date);
			e.printStackTrace();
		}
	}

	private void saveCommentedCommentsInfo(Connection c, ConcurrentLinkedQueue<String> queue, int start, int end) {
		PreparedStatement ps = null;
		int commentAuthorId = 0, postAuthorId = 0;
		double sentiment1 = 0, sentiment2 = 0;
		LocalDateTime date = null;
		try {
			ps = c.prepareStatement(selectCommentsForCommentsForGivenIds);
			ps.setInt(1, start);
			ps.setInt(2, end);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				commentAuthorId = rs.getInt(1);
				postAuthorId = rs.getInt(2);
				sentiment1 = rs.getDouble("sum1");
				sentiment2 = rs.getDouble("sum2");
				date = LocalDateTime.ofInstant(rs.getTimestamp("date").toInstant(), ZoneId.systemDefault());
				queue.add(createInsert(commentAuthorId, postAuthorId, sentiment1, sentiment2, date));				
			}
		} catch(Exception e) {
			System.out.println("ERROR while processing data: "+commentAuthorId+" "+postAuthorId+" "+sentiment1+" "+sentiment2+" "+date);
			e.printStackTrace();
		}
	}


	private Runnable createRunnableCollectingData(final Connection c, final int start, final int end) {
		Instant startTime = Instant.now();
		runningThreads.incrementAndGet();
		Runnable aRunneble = new Runnable() {
			public void run() {
				try {	
					ConcurrentLinkedQueue <String> insertQueries = new ConcurrentLinkedQueue <String>();			
					System.out.println("Collecting info for range: "+start+" - " +end);
					saveCommentedPostsInfo(c, insertQueries, start, end);
					saveCommentedCommentsInfo(c, insertQueries, start, end);
					System.out.println("Inserting into database("+insertQueries.size()+" rows) info collected for range: "+start+" - " +end);
					insertDataIntoTable(c, insertQueries);
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					try {
						Instant endTime = Instant.now();
						System.out.println("FINISHED Collecting and saving info for range: "+start+" - " +end+
								", it took "+Duration.between(startTime, endTime).getSeconds()+" seconds");
						runningThreads.decrementAndGet();
						c.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			}
		};
		return aRunneble;
	}
	
	private void insertDataIntoTable(Connection c, ConcurrentLinkedQueue<String> queue) {
		Statement ps = null;
		int i =0;
		String query = null;
		try {
			ps= c.createStatement();
			while(!queue.isEmpty()) {
				query = queue.poll();
				if(query ==null) {
					break;
				}
				ps.addBatch(query);
				i++;
				if(queue.isEmpty()) {
					break;
				}
			}
			ps.executeBatch();
			c.commit();
			System.out.println("Executed number of inserts="+i);

		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			try {
				ps.close();
				c.close();	
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String createInsert(int id1, int id2, Double sent1, Double sent2, LocalDateTime date) {
		if(sent1.isNaN()) {
			sent1 = 0.0;
		}
		if(sent2.isNaN()) {
			sent2 = 0.0;
		}
		String dateString = date.toString().replace("T", " ");

		return "insert into users_interactions(source, target, weight, weight2, date) "
				+ "values ("+id1+", "+id2+", "+sent1+", "+sent2+", '"+dateString+"');";
	}

}
