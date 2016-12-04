package dataBaseAnalyzer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import repast.simphony.context.Context;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class DatabaseDataLoader {
	private AtomicInteger runningThreads = new AtomicInteger(0);
	private ExecutorService cachedPool = Executors.newCachedThreadPool();

	private String selectCommentsForPosts = 
			"SELECT distinct on (c.author_id, posts.author_id) c.author_id, posts.author_id, c.sentiment, c.sentiment2 "
			+ "FROM comments as c join " 
			+ "posts on c.post_id = posts.id where c.author_id Between ? and ? ;";
	
	public DatabaseDataLoader() {
	}
	
	public void getUsers(Connection c) {
       Statement stmt = null;
       try {
         stmt = c.createStatement();
         ResultSet rs = stmt.executeQuery( "SELECT * FROM AUTHORS ; " );
         User u = null;
         do{
            rs.next();
            int id = rs.getInt("id");
            u = new User(DatabaseAnalyzerContext.space, DatabaseAnalyzerContext.grid, id);
            DatabaseAnalyzerContext.mainContext.add(u);
            DatabaseAnalyzerContext.users.put(u.getId(), u);
         } while(!rs.isLast());
         rs.close();
         stmt.close();
       } catch ( Exception e ) {
    	   e.printStackTrace();
         System.exit(0);
       } finally {
    	   try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
       }
       System.out.println("Selecting users done successfully");
	}
	
	public void saveCommentedPostsInfo(Connection c, int start, int end) {
		PreparedStatement ps = null;
		int commentAuthorId = 0, postAuthorId = 0;
		double sentiment1 = 0, sentiment2 = 0;
		try {
			ps = c.prepareStatement(selectCommentsForPosts);
			ps.setInt(1, start);
			ps.setInt(2, end);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				commentAuthorId = rs.getInt(1);
				postAuthorId = rs.getInt(2);
				sentiment1 = normalizeValue(rs.getDouble("sentiment"));
				sentiment2 = normalizeValue(rs.getDouble("sentiment2"));
				createInsert(commentAuthorId, postAuthorId, sentiment1, sentiment2);				
	        }
		} catch(Exception e) {
			System.out.println(commentAuthorId+" "+postAuthorId+" "+sentiment1+" "+sentiment2);
			e.printStackTrace();
		}
	}
	
	
	private Runnable createRunnable(final Connection c) {
		runningThreads.incrementAndGet();
		Runnable aRunneble = new Runnable() {
			public void run() {
				try {				
					int start = indexStart.get();
					int end = indexStart.addAndGet(BATCH_SIZE);
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
}
