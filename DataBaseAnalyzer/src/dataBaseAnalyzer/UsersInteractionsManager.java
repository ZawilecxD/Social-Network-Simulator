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
	private ConcurrentLinkedQueue <String> insertQueries = new ConcurrentLinkedQueue <String>();
	private AtomicInteger indexStart = new AtomicInteger(0);
	private int singleSelectSize = 10000;
	private int singleInsetBatchSize = 10000;

	private final String selectCommentsForPostsForGivenIds = 
			"select c.author_id, p.author_id, SUM(c.sentiment) as sum1, SUM(c.sentiment2) as sum2"
					+" from comments c join posts p on c.post_id = p.id "
					+" where c.id BETWEEN ? AND ? "
					+" group by c.author_id,p.author_id;";

	private final String selectCommentsForCommentsForGivenIds = 
			"select c.author_id, c2.author_id, SUM(c.sentiment) as sum1, SUM(c.sentiment2) as sum2, c.date "
					+"from comments c join comments c2 on c.parentcomment_id = c2.id " 
					+" where c.id BETWEEN ? AND ? "
					+"group by c.author_id, c2.author_id, c.date;";



	public UsersInteractionsManager(int singleSelectSize, int singleInsetBatchSize) {
		this.singleSelectSize = singleSelectSize;
		this.singleInsetBatchSize = singleInsetBatchSize;
	}

	public void collectInfoAndSaveInDatabase() {
		Instant start = Instant.now();
		User u = null;
		while(indexStart.get() <= DatabaseAnalyzerContext.getUsers().keySet().size()) {
			if(runningThreads.get() == DatabaseAnalyzerContext.MAX_THREADS_NUMBER) {
				continue;
			}
			cachedPool.execute(createRunnableCollectingData(DatabaseAnalyzerContext.databaseConnection()));
		};

		try {
			cachedPool.shutdown();
			cachedPool.awaitTermination(1, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		System.out.println("POOL SHUTDOWN!!!");

		Connection c = DatabaseAnalyzerContext.databaseConnection();
		Statement ps = null;
		int i =0;
		String query = null;
		try {
			ps= c.createStatement();
			while(!insertQueries.isEmpty()) {
				query = insertQueries.poll();
				if(query ==null) {
					break;
				}
				ps.addBatch(query);
				i++;
				if(i % singleInsetBatchSize == 0) {
					ps.executeBatch();
					ps.close();
					ps = c.createStatement();
					System.out.println("Number of inserts left "+insertQueries.size());
				}
				if(insertQueries.isEmpty()) {
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
				Instant end = Instant.now();
				System.out.println("Collecting users interactions info and saving it in database took: "+Duration.between(start, end).getSeconds()+" seconds");
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void saveCommentedPostsInfo(Connection c, int start, int end) {
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
				date = LocalDateTime.ofInstant(rs.getDate("date").toInstant(), ZoneId.systemDefault());
				System.out.println(date);
				createInsert(commentAuthorId, postAuthorId, sentiment1, sentiment2, date);				
			}
		} catch(Exception e) {
			System.out.println("ERROR while processing data: "+commentAuthorId+" "+postAuthorId+" "+sentiment1+" "+sentiment2+" "+date.toString());
			e.printStackTrace();
		}
	}

	private void saveCommentedCommentsInfo(Connection c, int start, int end) {
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
				date = LocalDateTime.ofInstant(rs.getDate("date").toInstant(), ZoneId.systemDefault());
				createInsert(commentAuthorId, postAuthorId, sentiment1, sentiment2, date);				
			}
		} catch(Exception e) {
			System.out.println("ERROR while processing data: "+commentAuthorId+" "+postAuthorId+" "+sentiment1+" "+sentiment2+" "+date.toString());
			e.printStackTrace();
		}
	}


	private Runnable createRunnableCollectingData(final Connection c) {
		runningThreads.incrementAndGet();
		Runnable aRunneble = new Runnable() {
			public void run() {
				try {				
					int start = indexStart.get();
					int end = indexStart.addAndGet(singleSelectSize);
					System.out.println("Collecting info for range: "+start+" - " +end);
					saveCommentedPostsInfo(c, start, end);
					saveCommentedCommentsInfo(c, start, end);
					System.out.println("FINISHED Collecting info for range: "+start+" - " +end);
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					try {
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

	private void createInsert(int id1, int id2, Double sent1, Double sent2, LocalDateTime date) {
		if(sent1.isNaN()) {
			sent1 = 0.0;
		}
		if(sent2.isNaN()) {
			sent2 = 0.0;
		}

		String query = "insert into users_interactions(source, target, weight, weight2, date) "
				+ "values ("+id1+", "+id2+", "+sent1+", "+sent2+", "+date.toString()+");";
		insertQueries.add(query);
	}

}
