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
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import user.Sex;
import user.User;
import user.UserCharacteristics;

public class SocialNetworkContext implements ContextBuilder<Object> {

	private static final Logger logger = Logger.getRootLogger();
	public final static int MINIMAL_SESSION_TIME =  15; //minimum of 15 minutes (1 tick = 1 minute)
	public final static int DAY_LENGTH_IN_TICKS = 1440; 
	public final static int CHANCE_TO_MEET_RELATIVES = 5; //chance (in percents) to find relatives on fb
	
	private static AtomicInteger usersId;
	private static AtomicInteger postsId;
	private static AtomicInteger groupsId;
	private static AtomicInteger eventsId;
	
	private @Getter static HashMap<Integer, User> usersMap; 
	private @Getter static HashMap<Integer, Post> postsMap; 
	private @Getter static HashMap<Integer, Group> groupsMap; 
	
	@Override
	public Context<Object> build(Context<Object> context) {
		BasicConfigurator.configure();
		context.setId("SocialNetworkSimulation");
		initIds();
		initCollections();
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("friendships network", context, true);
		netBuilder.buildNetwork();
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		int usersNumber = (Integer) params.getValue("usersNumber");
		
		for(int i=0; i<usersNumber; i++) {
			User newUser = new User(context, Sex.MALE, UserCharacteristics.defaultCharacteristics());
			context.add(newUser);
			newUser.addToFavouriteTags(Tag.FOOD);
			newUser.addToFavouriteTags(Tag.FOOTBALL);
			newUser.addToFavouriteTags(Tag.ADVENTURE);
			getUsersMap().put(newUser.getUserId(), newUser);
		}

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
	}
	
	private void initCollections() {
		usersMap = new HashMap<Integer, User>();
		postsMap = new HashMap<Integer, Post>();
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
	
	public static int getUsersCount() {
		return usersMap.keySet().size();
	}

}
