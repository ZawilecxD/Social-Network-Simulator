package dataBaseAnalyzer;

import java.util.HashMap;

import org.apache.log4j.Logger;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameter;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class User  {
	private ContinuousSpace <Object> space;
	private Grid<Object> grid;
	private int id;
	
//	private HashMap<Integer, Double> edges;

	
	 @Parameter (displayName = "ID", usageName = "id" )
	 public int getId() {
	        return id;
	 }
	 
	public void linkToOther(User user2, double weight) {
		DatabaseAnalyzerContext.getNetwork().addEdge(this, user2).setWeight(weight);
	}
	
//	public void createEdges() {
//		User user2 = null;
//		for(int key : edges.keySet()) {
//			user2 = DatabaseAnayzerContext.users.get(key);
//			DatabaseAnayzerContext.getNetwork().addEdge(this, user2).setWeight(edges.get(key));
//		}
//		edges.clear();
//	}

	public User(ContinuousSpace<Object> space, Grid<Object> grid, int id) {
		this.space = space;
		this.grid = grid;
		this.id = id;
//		this.edges = new HashMap<>();
	}

	@ScheduledMethod(start = 1, interval = 100)
	public void doit() {
		//System.out.println("ye");
	}

	@Override
	public String toString() {
		return "User [id=" + id + "]";
	}

//	public void addEdge(Integer otherUserId, Double weight ) {
//		edges.put(otherUserId, weight);
//	}

}