package dataBaseAnalyzer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;

/**
 * Used to get all authors(nodes) from database and all their interactions (edges).
 * This data will be used to build network in repast.
 * @author Murzynas
 *
 */
public class NetworkDataLoader {

	private final String selectAuthors = "SELECT * FROM AUTHORS ;";
	private final String selectUsersInteractions = "SELECT * FROM users_interactions;";

	public NetworkDataLoader() {

	}

	//NetworkDataLoader
	public void getUsers(Connection c) {
		Instant start = Instant.now();
		Statement stmt = null;
		try {
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery(selectAuthors);
			User u = null;
			do{
				rs.next();
				int id = rs.getInt("id");
				u = new User(DatabaseAnalyzerContext.space, DatabaseAnalyzerContext.grid, id);
				DatabaseAnalyzerContext.mainContext.add(u);
				DatabaseAnalyzerContext.getUsers().put(u.getId(), u);
			} while(!rs.isLast());
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				Instant end = Instant.now();
				System.out.println("Getting users from database took: "+Duration.between(start, end).toMillis()+" miliseconds");
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Users loaded from database successfully. They were added to context!");
	}

	//NetworkDataLoader
	public void getEdges() {
		System.out.println("START: "+selectUsersInteractions);
		Instant start = Instant.now();
		Connection c = DatabaseAnalyzerContext.databaseConnection();
		Statement stmt = null;
		try {
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( selectUsersInteractions );
			User u1 = null, u2 = null;
			int fromId, toId; 
			double sentiment, sentiment2;
			do{
				rs.next();
				fromId = rs.getInt("source");
				toId = rs.getInt("target");
				sentiment = rs.getDouble("weight");
				sentiment2 = rs.getDouble("weight2");
//				Date date = rs.getDate("date");
//				String type = rs.getType("type");
				u1 = DatabaseAnalyzerContext.getUsers().get(fromId);
				u2 = DatabaseAnalyzerContext.getUsers().get(toId);
				if(DatabaseAnalyzerContext.USED_SENTIMENT == 2) {
					DatabaseAnalyzerContext.addEdge(u1, u2, sentiment2);
				} else {
					DatabaseAnalyzerContext.addEdge(u1, u2, sentiment);
				}
			} while(!rs.isLast());
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
			e.printStackTrace();
			System.exit(0);
		} finally {
			try {
				Instant end = Instant.now();
				System.out.println("Getting users interactions from database took: "+Duration.between(start, end).getSeconds()+" seconds");
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Users Interactions loaded from database successfully. Edges added to context!");
	}	

}
