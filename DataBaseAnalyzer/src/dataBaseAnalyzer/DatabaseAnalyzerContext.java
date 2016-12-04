package dataBaseAnalyzer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.ContextJungNetwork;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.graph.DirectedJungNetwork;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
 
public class DatabaseAnalyzerContext  implements ContextBuilder<Object>{
	public static Context<Object> mainContext;
	public static ContinuousSpace<Object> space;
	public static Grid<Object> grid;
	public static HashMap<Integer, User> users = new HashMap<>();
	
	public static final int SENTIMENT_NUMBER = 1;
	public static final int AUTHOR_INDEX_LIMIT = 32000; 
	public static final int BATCH_SIZE= 10000;
	public static final int MAX_THREADS_NUMBER = 8;
    private ExecutorService cachedPool = Executors.newFixedThreadPool(MAX_THREADS_NUMBER);
    private AtomicInteger runningThreads = new AtomicInteger(0);
    
    public static ContextJungNetwork<Object> contextNet;
    
    private AtomicInteger indexStart = new AtomicInteger(0);
    private ConcurrentLinkedQueue <String> insertQueries = new ConcurrentLinkedQueue <String>();
	
	@Override
	public Context build(Context<Object> context) {
		mainContext = context;
		mainContext.setId("DataBaseAnalyzer");
		
		Instant start = Instant.now();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		
		space = spaceFactory.createContinuousSpace("space", mainContext, 
				new RandomCartesianAdder <Object>(), 
				new repast.simphony.space.continuous.WrapAroundBorders (), 500 , 500);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		grid = gridFactory.createGrid("grid", mainContext, 
				new GridBuilderParameters<Object>(new repast.simphony.space.grid.WrapAroundBorders(),
				new SimpleGridAdder<Object>() ,
				true , 500 , 500));
		
//		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("friendships network", mainContext, true);
//		netBuilder.buildNetwork();
		
		DirectedJungNetwork<Object> jungNet = new DirectedJungNetwork<>("friendships network");	
		contextNet = new ContextJungNetwork<>(jungNet, mainContext);
		
		System.out.println(contextNet.isDirected());
		
		for(Object obj : mainContext) {
			NdPoint point = space.getLocation(obj);
			grid.moveTo(obj, (int)point.getX(), (int)point.getY());
		}
		
		getUsers(databaseConnection());
		Instant endGetUsers = Instant.now();
	    System.out.println("Getting "+users.size()+" users from database took: "+Duration.between(start, endGetUsers).toMillis()+" ms");
	     
//	    Instant startBuildNetwork = Instant.now();
//	    collectInfoAndSaveInDatabase();
//		Instant endBuildNetwork = Instant.now();
//	    System.out.println("Building Network took: "+Duration.between(startBuildNetwork, endBuildNetwork).getSeconds()+" seconds");
//		
	    Instant startGettingEdges = Instant.now();
	    getEdges();
		Instant endGettingEdges = Instant.now();
	    System.out.println("Getting edges form database took: "+Duration.between(startGettingEdges, endGettingEdges).getSeconds()+" seconds");

	    System.out.println("LICZBA EDGY TO: "+contextNet.numEdges());
	    System.out.println("DEGREE OF USER 2 =" +contextNet.getDegree(users.get(2)));

	    BetweennessCentrality<Object, RepastEdge<Object>>  bc = new BetweennessCentrality<>(contextNet.getGraph());
	    System.out.println("BC ="+bc.getVertexScore(users.get(2)));
	    
		return mainContext;
	}
	
	public static Connection databaseConnection() {
		 Connection c = null;
		 try {
			 Class.forName("org.postgresql.Driver");
	         c = DriverManager
	            .getConnection("jdbc:postgresql://localhost:5432/salon24db",
	            "postgres", "");
	         c.setAutoCommit(false);
	         System.out.println("Opened database successfully");
	         return c;
	       } catch ( Exception e ) {
	    	   e.printStackTrace();
	         System.exit(0);
	         return null;
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
	
	private void createInsert(int id1, int id2, Double sent1, Double sent2) {
		if(sent1.isNaN()) {
			sent1 = 0.0;
		}
		if(sent2.isNaN()) {
			sent2 = 0.0;
		}
		String query = "insert into temp_network_data(source, target, weight, weight2) values ("+id1+", "+id2+", "+sent1+", "+sent2+");";
		insertQueries.add(query);
	}
	
	private double normalizeValue(double value) {
		value = value + 1;
		return value/2.0;
	}
	
	
	public void saveCommentedPostsInfo(Connection c, int start, int end) {
		PreparedStatement ps = null;
		int commentAuthorId = 0, postAuthorId = 0;
		double sentiment1 = 0, sentiment2 = 0;
		try {
			ps = c.prepareStatement("SELECT distinct on (c.author_id, posts.author_id) c.author_id, posts.author_id, c.sentiment, c.sentiment2 FROM comments as c join "
					+ "posts on c.post_id = posts.id where c.author_id Between ? and ? ;");
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
	
	public void saveCommentedCommentsInfo(Connection c, int start, int end) {
		PreparedStatement ps = null;
		int commentAuthorId = 0, parentCommentAuthorId = 0;
		double sentiment1 = 0, sentiment2 = 0;
		try {
			ps = c.prepareStatement("SELECT distinct on (c1.author_id, c2.author_id) c1.author_id, c2.author_id, c1.sentiment, c1.sentiment2  "
					+ "FROM comments as c1 join comments as c2 on "
					+ "c1.parentcomment_id=c2.id "
					+ "where c1.author_id BETWEEN ? and ? ;");
			ps.setInt(1, start);
			ps.setInt(2, end);
			ResultSet rs = ps.executeQuery();
			while(rs.next()){
				commentAuthorId = rs.getInt(1);
				parentCommentAuthorId = rs.getInt(2);
				sentiment1 = normalizeValue(rs.getDouble("sentiment"));
				sentiment2 = normalizeValue(rs.getDouble("sentiment2"));
				createInsert(commentAuthorId, parentCommentAuthorId, sentiment1, sentiment2);
	        } 
		} catch(Exception e) {
			System.out.println(commentAuthorId+" "+parentCommentAuthorId+" "+sentiment1+" "+sentiment2);
			e.printStackTrace();
		}
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
            u = new User(space, grid, id);
            mainContext.add(u);
            users.put(u.getId(), u);
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
	
	private void getEdges() {
		Connection c = databaseConnection();
	  Statement stmt = null;
       try {
         stmt = c.createStatement();
         ResultSet rs = stmt.executeQuery( "SELECT * FROM temp_network_data;" );
         User u1 = null;
         User u2 = null;
         do{
            rs.next();
            int fromId = rs.getInt("source");
            int toId = rs.getInt("target");
            double sentiment = rs.getDouble("weight");
            double sentiment2 = rs.getDouble("weight2");
            u1 = users.get(fromId);
            u2 = users.get(toId);
            if(SENTIMENT_NUMBER == 1) {
            	contextNet.addEdge(u1, u2).setWeight(sentiment);
            } else {
            	contextNet.addEdge(u1, u2).setWeight(sentiment2);
            }
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
	
	private void collectInfoAndSaveInDatabase() {
		User u = null;
		while(users.keySet().size() - indexStart.get() >=0) {
			 if(runningThreads.get() == MAX_THREADS_NUMBER || indexStart.get() >= AUTHOR_INDEX_LIMIT) {
	             	continue;
             }
			cachedPool.execute(createRunnable(databaseConnection()));
		};
		System.out.println("FINISHED BUILDING NETWORK");
		
		try {
			cachedPool.shutdown();
			cachedPool.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		System.out.println("POOL SHUTDOWN!!!");
		
		Connection c = databaseConnection();
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
				if(i % BATCH_SIZE == 0) {
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
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
		
	
	public static Network<Object> getNetwork() {
		return (Network<Object>) mainContext.getProjection("friendships network");
	}

}
