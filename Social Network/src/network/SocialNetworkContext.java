package network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import lombok.Getter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import posts.Post;
import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import user.Sex;
import user.User;
import user.UserCharacteristics;

public class SocialNetworkContext implements ContextBuilder<Object> {

	private static final Logger logger = Logger.getRootLogger();
	public final static int MINIMAL_SESSION_TIME =  15; //minimum of 15 minutes (1 tick = 1 minute)
	public final static int DAY_LENGTH_IN_TICKS = 1440; 
	public final static int CHANCE_TO_MEET_RELATIVES = 5; //chance (in percents) to find relatives on fb
	public final static int MINIMAL_FRIENDS_COUNT = 10;
	public final static int MINIMAL_VALUE_OF_FRIEND = 1000;
	public final static int MAX_EVENT_TIME = 5;
	public final static int MAX_GROUPS_PER_OWNER = 3;
	public final static int MAX_FOUND_GROUPS = 50;
	public final static int MAX_FOUND_POSTS = 50;
	public final static int MAX_FOUND_EVENTS = 25;
	
	private static AtomicInteger usersId;
	private static AtomicInteger postsId;
	private static AtomicInteger groupsId;
	private static AtomicInteger eventsId;
	
	private @Getter static HashMap<Integer, User> usersMap; 
	private @Getter static HashMap<Integer, Post> postsMap; 
	private @Getter static HashMap<Integer, Group> groupsMap; 
	private @Getter static HashMap<Integer, Event> eventsMap; 
	
	private static Context mainContext;
	
	public static Context getMainContext() {
		return mainContext;
	}
	
	@Override
	public Context build(Context<Object> context) {
//		BasicConfigurator.configure();
		context.setId("Social Network");
		initIds();
		initCollections();
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space", context, 
				new RandomCartesianAdder <Object>(), 
				new repast.simphony.space.continuous.WrapAroundBorders ()
				, 50 , 50);
		
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context, 
				new GridBuilderParameters<Object>(new repast.simphony.space.grid.WrapAroundBorders(),
				new SimpleGridAdder<Object>() ,
				true , 50 , 50));
		
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("friendships network", context, true);
		netBuilder.buildNetwork();
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		int usersNumber = (Integer) params.getValue("usersNumber");
		
		for(int i=0; i<usersNumber; i++) {
			User newUser = new User(space, grid, Sex.MALE, UserCharacteristics.defaultCharacteristics());
			context.add(newUser);
			newUser.addToFavouriteTags(Tag.FOOD);
			newUser.addToFavouriteTags(Tag.FOOTBALL);
			newUser.addToFavouriteTags(Tag.ADVENTURE);
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for(Object obj : context) {
			NdPoint point = space.getLocation(obj);
			grid.moveTo(obj, (int)point.getX(), (int)point.getY());
		}

		mainContext = context;
		return context;
	}
	
	public static int getCurrentTick() {
		return (int) Math.ceil(RunEnvironment.getInstance().getCurrentSchedule().getTickCount());
	}
	
	private void initIds() {
		usersId = new AtomicInteger();
		usersId.set(0);
		postsId = new AtomicInteger();
		postsId.set(0);
		groupsId = new AtomicInteger();
		groupsId.set(0);
		eventsId = new AtomicInteger();
		eventsId.set(0);
	}
	
	private void initCollections() {
		usersMap = new HashMap<>();
		postsMap = new HashMap<>();
		groupsMap = new HashMap<>();
		eventsMap = new HashMap<>();
	}
	
	public static int getNextUserId() {
		return usersId.getAndIncrement();
	}
	
	public static int getNextPostId() {
		return postsId.getAndIncrement();
	}
	
	public static int getNextGroupId() {
		return groupsId.getAndIncrement();
	}
	
	public static int getNextEventId() {
		return eventsId.getAndIncrement();
	}
	
	public static void addPost(Post post) {
		postsMap.put(post.getPostId(), post);
	}
	
	public static void addGroup(Group group) {
		groupsMap.put(group.getGroupId(), group);
	}
	
	public static void addEvent(Event event) {
		eventsMap.put(event.getEventId(), event);
	}

	public static HashMap<Integer, User> getUsersMap() {
		return usersMap;
	}
	
	public static User getUserById(int id) {
		return usersMap.get(id);
	}
	
	public static Post getPostById(int id) {
		return postsMap.get(id);
	}
	
	public static Group getGroupById(int id) {
		return groupsMap.get(id);
	}
	
	public static Event getEventById(int id) {
		return eventsMap.get(id);
	}
	
	public static int getUsersCount() {
		return usersMap.keySet().size();
	}

}
