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
	private static HashMap<Integer, User> users = new HashMap<>();

	public static int USED_SENTIMENT = 1;
	public static final int MAX_THREADS_NUMBER = 2;    
	public static ContextJungNetwork<Object> contextNet;

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

		DirectedJungNetwork<Object> jungNet = new DirectedJungNetwork<>("friendships network");	
		contextNet = new ContextJungNetwork<>(jungNet, mainContext);

		NetworkDataLoader dataLoader = new NetworkDataLoader();
		UsersInteractionsManager userInterManager = new UsersInteractionsManager(10000, 10000);
		dataLoader.getUsers(databaseConnection());

		userInterManager.collectInfoAndSaveInDatabase();
			
//		dataLoader.getEdges();

		System.out.println("Liczba wêz³ów w sieci: "+contextNet.size());
		System.out.println("Liczba krawêdzi w sieci: "+contextNet.numEdges());
//		System.out.println("DEGREE OF USER 2 =" +contextNet.getDegree(users.get(2)));

//		BetweennessCentrality<Object, RepastEdge<Object>>  bc = new BetweennessCentrality<>(contextNet.getGraph());
//		System.out.println("BC ="+bc.getVertexScore(users.get(2)));

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

	private void createGridPoints() {
		for(Object obj : mainContext) {
			NdPoint point = space.getLocation(obj);
			grid.moveTo(obj, (int)point.getX(), (int)point.getY());
		}
	}

	public static HashMap<Integer, User> getUsers() {
		return users;
	}

	public static void addToUsers(User u) {
		users.put(u.getId(), u);
	}

	public static Network<Object> getNetwork() {
		return (Network<Object>) mainContext.getProjection("friendships network");
	}

}
