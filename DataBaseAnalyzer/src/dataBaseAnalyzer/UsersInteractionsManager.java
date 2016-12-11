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
import java.util.concurrent.ArrayBlockingQueue;
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
	private ArrayBlockingQueue<String> insertsQueries = new ArrayBlockingQueue<>(500000);
	private boolean poolShutdowned = false;
	private int singleSelectSize = 5000;
	private int singleInsertBatchSize = 10000;

	private final String selectCommentsForPostsForGivenIds = 
			"select c.author_id, p.author_id, c.sentiment , c.sentiment2, c.date "
			+ "from comments c join posts p on c.post_id = p.id "
			+ "where c.id between ? and ?;";

	private final String selectCommentsForCommentsForGivenIds = 
			"select c.author_id, c2.author_id, c.sentiment , c.sentiment2, c.date "
			+ "from comments c join comments c2 on c.parentcomment_id = c2.id "
			+ "where c.id between ? and ?;";



	public UsersInteractionsManager(int singleSelectSize, int singleInsertBatchSize) {
		this.singleSelectSize = singleSelectSize;
		this.singleInsertBatchSize = singleInsertBatchSize;
	}
	
	private int selectCommentsCount() {
		Connection c = DatabaseAnalyzerContext.databaseConnection();
		int result = 0;
		try {
			PreparedStatement ps = c.prepareStatement("select count(*) from comments;");
			ResultSet rs = ps.executeQuery();
			rs.next();
			result = rs.getInt("count");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				c.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Number of rows in comments table = "+result);
		return result;
	}
	
	public void collectInfoAndSaveInDatabase() {
		User u = null;
		int start = 0, end = 0;
		int commentsCount = selectCommentsCount() ; //DatabaseAnalyzerContext.getUsers().keySet().size();
		
		runningThreads.incrementAndGet();
		
		cachedPool.execute(createInsertingRunnable());
		
		while(start < commentsCount) {
			if(runningThreads.get() == DatabaseAnalyzerContext.MAX_THREADS_NUMBER) {
				continue;
			}
			start = indexStart.get();
			end = indexStart.addAndGet(singleSelectSize);
			if(start > commentsCount) {
				break;
			}
			cachedPool.execute(createRunnableCollectingData(DatabaseAnalyzerContext.databaseConnection(), start, end));
		};
		

		try {
			cachedPool.shutdown();
			cachedPool.awaitTermination(1, TimeUnit.HOURS);
			poolShutdowned = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		System.out.println("POOL SHUTDOWN!!!");
		
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
				sentiment1 = rs.getDouble("sentiment");
				sentiment2 = rs.getDouble("sentiment2"); 
				date = LocalDateTime.ofInstant(rs.getTimestamp("date").toInstant(), ZoneId.systemDefault());
				insertsQueries.put(createInsert(commentAuthorId, postAuthorId, sentiment1, sentiment2, date, "commentToPost"));				
			}	
		} catch(Exception e) {
			System.out.println("ERROR while processing data: "+commentAuthorId+" "+postAuthorId+" "+sentiment1+" "+sentiment2+" "+date);
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
				sentiment1 = rs.getDouble("sentiment");
				sentiment2 = rs.getDouble("sentiment2");
				date = LocalDateTime.ofInstant(rs.getTimestamp("date").toInstant(), ZoneId.systemDefault());
				insertsQueries.put(createInsert(commentAuthorId, postAuthorId, sentiment1, sentiment2, date, "commentToComment"));				
			}
		} catch(Exception e) {
			System.out.println("ERROR while processing data: "+commentAuthorId+" "+postAuthorId+" "+sentiment1+" "+sentiment2+" "+date);
			e.printStackTrace();
		}
	}

	private Runnable createInsertingRunnable() {
		Runnable aRunnable = new Runnable() {
			public void run() {
				insertDataIntoTable(DatabaseAnalyzerContext.databaseConnection());
				runningThreads.decrementAndGet();
			}
		};
		return aRunnable;
	}

	private Runnable createRunnableCollectingData(final Connection c, final int start, final int end) {
		Instant startTime = Instant.now();
		runningThreads.incrementAndGet();
		Runnable aRunneble = new Runnable() {
			public void run() {
				try {	
					System.out.println("Collecting info for range: "+start+" - " +end);
					saveCommentedPostsInfo(c, start, end);
					saveCommentedCommentsInfo(c, start, end);
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
	
	private void insertDataIntoTable(Connection c) {
		System.out.println("Started inserting");
		Statement ps = null;
		int i =0;
		String query = null;
		try {
			ps= c.createStatement();
			while(true) {
				if(insertsQueries.isEmpty() && runningThreads.get() == 1) {
					break;
				}
				
				query = insertsQueries.take();
				if(query == null) {
					continue;
				}
				
				ps.addBatch(query);
				i++;
				
				if(i % singleInsertBatchSize == 0) {
					ps.executeBatch();
					ps.close();
					c.commit();
					ps = c.createStatement();
					System.out.println("Number of inserts left "+insertsQueries.size());
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

	private String createInsert(int id1, int id2, Double sent1, Double sent2, LocalDateTime date, String operationType) {
		if(sent1.isNaN()) {
			sent1 = 0.0;
		}
		if(sent2.isNaN()) {
			sent2 = 0.0;
		}
		String dateString = date.toString().replace("T", " ");

		return "insert into users_interactions(source, target, weight, weight2, date, type) "
				+ "values ("+id1+", "+id2+", "+sent1+", "+sent2+", '"+dateString+"', '"+operationType+"');";
	}

}
