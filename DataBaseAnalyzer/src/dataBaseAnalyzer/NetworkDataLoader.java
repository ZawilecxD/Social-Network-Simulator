package dataBaseAnalyzer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;

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
				System.out.println("Getting users from database took: "+Duration.between(start, end).getSeconds()+" seconds");
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Users loaded from database successfully. They were added to context!");
	}

	//NetworkDataLoader
	public void getEdges() {
		Instant start = Instant.now();
		Connection c = DatabaseAnalyzerContext.databaseConnection();
		Statement stmt = null;
		try {
			stmt = c.createStatement();
			ResultSet rs = stmt.executeQuery( selectUsersInteractions );
			User u1 = null;
			User u2 = null;
			do{
				rs.next();
				int fromId = rs.getInt("source");
				int toId = rs.getInt("target");
				double sentiment = rs.getDouble("weight");
				double sentiment2 = rs.getDouble("weight2");
				u1 = DatabaseAnalyzerContext.getUsers().get(fromId);
				u2 = DatabaseAnalyzerContext.getUsers().get(toId);
				if(DatabaseAnalyzerContext.USED_SENTIMENT == 2) {
					DatabaseAnalyzerContext.contextNet.addEdge(u1, u2).setWeight(sentiment2);
				} else {
					DatabaseAnalyzerContext.contextNet.addEdge(u1, u2).setWeight(sentiment);
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
				System.out.println("Getting users from database took: "+Duration.between(start, end).getSeconds()+" seconds");
				c.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Users Interactions loaded from database successfully. Edges added to context!");
	}	

}
