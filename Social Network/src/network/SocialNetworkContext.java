package network;

import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;

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
import repast.simphony.ui.RSApplication;
import repast.simphony.ui.probe.Probe;
import user.Sex;
import user.User;
import user.UserCharacteristics;

public class SocialNetworkContext implements ContextBuilder<Object> {

	private static final Logger logger = Logger.getRootLogger();
	public final static int MINIMAL_SESSION_TIME =  15; //minimum of 15 minutes (1 tick = 1 minute)
	public final static int DAY_LENGTH_IN_TICKS = 1440; 
	public final static int CHANCE_TO_MEET_RELATIVES = 5; //chance (in percents) to find relatives on fb
	public final static int MINIMAL_FRIENDS_COUNT = 10;
	public final static int MINIMAL_VALUE_OF_FRIEND = 100;
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
	
	private HashMap<Integer, Integer> tagMap = new HashMap<Integer, Integer>();
	
	private static Context mainContext;
	
	public static Context getMainContext() {
		return mainContext;
	}
	
	public static void testUserPanel(String value) {
		System.out.println("UserPanel setn value = "+value);
		usersMap.clear();
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
		
		JPanel newPanel = new JPanel(new GridBagLayout());
		JLabel label = new JLabel("Enter username:");
		newPanel.add(label);
		RSApplication.getRSApplicationInstance().addCustomUserPanel(newPanel);
		
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("friendships network", context, true);
		netBuilder.buildNetwork();
		
		Parameters params = RunEnvironment.getInstance().getParameters();
		int usersNumber = (Integer) params.getValue("usersNumber");
		
		int likerNumber = (int)(usersNumber * 0.2);
		int commentatorNumber = (int)(usersNumber * 0.17);
		int chatterNumber = (int)(usersNumber * 0.14);
		int groupieNumber = (int)(usersNumber * 0.12);
		int nerdNumber = (int)(usersNumber * 0.09);
		int haterNumber = (int)(usersNumber * 0.09);
		int observerNumber = (int)(usersNumber * 0.08);
		int nervousNumber = (int)(usersNumber * 0.07);
		int lonerNumber = usersNumber - likerNumber - commentatorNumber - chatterNumber - groupieNumber - nerdNumber
				- haterNumber - observerNumber - nervousNumber;
		
		int fullTagNumber = 6 * usersNumber;
		
		tagMap.put(Integer.valueOf(1), Integer.parseInt(String.valueOf((int)(0.1 * fullTagNumber))));
		tagMap.put(Integer.valueOf(2), Integer.parseInt(String.valueOf((int)(0.09 * fullTagNumber))));
		tagMap.put(Integer.valueOf(3), Integer.parseInt(String.valueOf((int)(0.07 * fullTagNumber))));
		tagMap.put(Integer.valueOf(4), Integer.parseInt(String.valueOf((int)(0.05 * fullTagNumber))));
		tagMap.put(Integer.valueOf(5), Integer.parseInt(String.valueOf((int)(0.05 * fullTagNumber))));
		tagMap.put(Integer.valueOf(6), Integer.parseInt(String.valueOf((int)(0.04 * fullTagNumber))));
		tagMap.put(Integer.valueOf(7), Integer.parseInt(String.valueOf((int)(0.04 * fullTagNumber))));
		tagMap.put(Integer.valueOf(8), Integer.parseInt(String.valueOf((int)(0.03 * fullTagNumber))));
		tagMap.put(Integer.valueOf(9), Integer.parseInt(String.valueOf((int)(0.03 * fullTagNumber))));
		tagMap.put(Integer.valueOf(10), Integer.parseInt(String.valueOf((int)(0.03 * fullTagNumber))));
		tagMap.put(Integer.valueOf(11), Integer.parseInt(String.valueOf((int)(0.03 * fullTagNumber))));
		tagMap.put(Integer.valueOf(12), Integer.parseInt(String.valueOf((int)(0.03 * fullTagNumber))));
		tagMap.put(Integer.valueOf(13), Integer.parseInt(String.valueOf((int)(0.03 * fullTagNumber))));
		tagMap.put(Integer.valueOf(14), Integer.parseInt(String.valueOf((int)(0.03 * fullTagNumber))));
		tagMap.put(Integer.valueOf(15), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(16), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(17), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(18), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(19), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(20), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(21), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(22), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(23), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(24), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(25), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(26), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(27), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(28), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(29), Integer.parseInt(String.valueOf((int)(0.02 * fullTagNumber))));
		tagMap.put(Integer.valueOf(30), Integer.parseInt(String.valueOf((int)(0.01 * fullTagNumber))));
		tagMap.put(Integer.valueOf(31), Integer.parseInt(String.valueOf((int)(0.01 * fullTagNumber))));
		tagMap.put(Integer.valueOf(32), Integer.parseInt(String.valueOf((int)(0.01 * fullTagNumber))));
		tagMap.put(Integer.valueOf(33), Integer.parseInt(String.valueOf((int)(0.01 * fullTagNumber))));
		tagMap.put(Integer.valueOf(34), Integer.parseInt(String.valueOf((int)(0.01 * fullTagNumber))));
		
		Random random = new Random();
		
//		for(int i=0; i<usersNumber; i++) {
//		Sex sex = null;
//		if (random.nextInt(100) < 64) {
//			sex = Sex.FEMALE;
//		} else {
//			sex = Sex.MALE;
//		}
//		User newUser = new User(space, grid, sex, UserCharacteristics.defaultCharacteristics());
//		context.add(newUser);
//		Set<Integer> keySet = tagMap.keySet();
//		ArrayList<Integer> keys = new ArrayList<Integer>();
//		keys.addAll(keySet);
//		Collections.shuffle(keys);
//		for (int j=0; j<3; j++) {
//			Integer key = keys.get(j);
//			Integer value = tagMap.get(key);
//			Tag tag = getById(key);
//			if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
//				tagMap.remove(key);
//			} else {
//				tagMap.put(key, value - Integer.valueOf(1));
//			}
//			newUser.addToFavouriteTags(tag);
//		}
//		usersMap.put(newUser.getUserId(), newUser);
//	}
		
		for (int i=0; i<likerNumber; i++) {
			Sex sex = null;
			if (random.nextInt(100) < 64) {
				sex = Sex.FEMALE;
			} else {
				sex = Sex.MALE;
			}
			User newUser = new User(space, grid, sex, UserCharacteristics.likerCharacteristics());
			context.add(newUser);
			for (int j=0; j<3; j++) {
				Set<Integer> keySet = tagMap.keySet();
				Integer[] keys = new Integer[keySet.size()];
				keySet.toArray(keys);
				int keysSize = keys.length;
				int index = random.nextInt(keysSize);
				Integer key = keys[index];
				Integer value = tagMap.get(key);
				Tag tag = getById(key);
				if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
					tagMap.remove(key);
				} else {
					tagMap.put(key, value - Integer.valueOf(1));
				}
				newUser.addToFavouriteTags(tag);
			}
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for (int i=0; i<commentatorNumber; i++) {
			Sex sex = null;
			if (random.nextInt(100) < 64) {
				sex = Sex.FEMALE;
			} else {
				sex = Sex.MALE;
			}
			User newUser = new User(space, grid, sex, UserCharacteristics.commentatorCharacteristics());
			context.add(newUser);
			Set<Integer> keySet = tagMap.keySet();
			ArrayList<Integer> keys = new ArrayList<Integer>();
			keys.addAll(keySet);
			Collections.shuffle(keys);
			for (int j=0; j<3; j++) {
				Integer key = keys.get(j);
				Integer value = tagMap.get(key);
				Tag tag = getById(key);
				if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
					tagMap.remove(key);
				} else {
					tagMap.put(key, value - Integer.valueOf(1));
				}
				newUser.addToFavouriteTags(tag);
			}
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for (int i=0; i<chatterNumber; i++) {
			Sex sex = null;
			if (random.nextInt(100) < 64) {
				sex = Sex.FEMALE;
			} else {
				sex = Sex.MALE;
			}
			User newUser = new User(space, grid, sex, UserCharacteristics.chatterCharacteristics());
			context.add(newUser);
			Set<Integer> keySet = tagMap.keySet();
			ArrayList<Integer> keys = new ArrayList<Integer>();
			keys.addAll(keySet);
			Collections.shuffle(keys);
			for (int j=0; j<3; j++) {
				Integer key = keys.get(j);
				Integer value = tagMap.get(key);
				Tag tag = getById(key);
				if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
					tagMap.remove(key);
				} else {
					tagMap.put(key, value - Integer.valueOf(1));
				}
				newUser.addToFavouriteTags(tag);
			}
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for (int i=0; i<groupieNumber; i++) {
			Sex sex = null;
			if (random.nextInt(100) < 64) {
				sex = Sex.FEMALE;
			} else {
				sex = Sex.MALE;
			}
			User newUser = new User(space, grid, sex, UserCharacteristics.groupieCharacteristics());
			context.add(newUser);
			Set<Integer> keySet = tagMap.keySet();
			ArrayList<Integer> keys = new ArrayList<Integer>();
			keys.addAll(keySet);
			Collections.shuffle(keys);
			for (int j=0; j<3; j++) {
				Integer key = keys.get(j);
				Integer value = tagMap.get(key);
				Tag tag = getById(key);
				if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
					tagMap.remove(key);
				} else {
					tagMap.put(key, value - Integer.valueOf(1));
				}
				newUser.addToFavouriteTags(tag);
			}
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for (int i=0; i<nerdNumber; i++) {
			Sex sex = null;
			if (random.nextInt(100) < 64) {
				sex = Sex.FEMALE;
			} else {
				sex = Sex.MALE;
			}
			User newUser = new User(space, grid, sex, UserCharacteristics.nerdCharacteristics());
			context.add(newUser);
			Set<Integer> keySet = tagMap.keySet();
			ArrayList<Integer> keys = new ArrayList<Integer>();
			keys.addAll(keySet);
			Collections.shuffle(keys);
			for (int j=0; j<3; j++) {
				Integer key = keys.get(j);
				Integer value = tagMap.get(key);
				Tag tag = getById(key);
				if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
					tagMap.remove(key);
				} else {
					tagMap.put(key, value - Integer.valueOf(1));
				}
				newUser.addToFavouriteTags(tag);
			}
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for(int i=0; i<haterNumber; i++) {
			Sex sex = null;
			if (random.nextInt(100) < 64) {
				sex = Sex.FEMALE;
			} else {
				sex = Sex.MALE;
			}
			User newUser = new User(space, grid, sex, UserCharacteristics.haterCharacteristics());
			context.add(newUser);
			Set<Integer> keySet = tagMap.keySet();
			ArrayList<Integer> keys = new ArrayList<Integer>();
			keys.addAll(keySet);
			Collections.shuffle(keys);
			for (int j=0; j<3; j++) {
				Integer key = keys.get(j);
				Integer value = tagMap.get(key);
				Tag tag = getById(key);
				if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
					tagMap.remove(key);
				} else {
					tagMap.put(key, value - Integer.valueOf(1));
				}
				newUser.addToFavouriteTags(tag);
			}
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for(int i=0; i<observerNumber; i++) {
			Sex sex = null;
			if (random.nextInt(100) < 64) {
				sex = Sex.FEMALE;
			} else {
				sex = Sex.MALE;
			}
			User newUser = new User(space, grid, sex, UserCharacteristics.observerCharacteristics());
			context.add(newUser);
			Set<Integer> keySet = tagMap.keySet();
			ArrayList<Integer> keys = new ArrayList<Integer>();
			keys.addAll(keySet);
			Collections.shuffle(keys);
			for (int j=0; j<3; j++) {
				Integer key = keys.get(j);
				Integer value = tagMap.get(key);
				Tag tag = getById(key);
				if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
					tagMap.remove(key);
				} else {
					tagMap.put(key, value - Integer.valueOf(1));
				}
				newUser.addToFavouriteTags(tag);
			}
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for(int i=0; i<nervousNumber; i++) {
			Sex sex = null;
			if (random.nextInt(100) < 64) {
				sex = Sex.FEMALE;
			} else {
				sex = Sex.MALE;
			}
			User newUser = new User(space, grid, sex, UserCharacteristics.nervousCharacteristics());
			context.add(newUser);
			Set<Integer> keySet = tagMap.keySet();
			ArrayList<Integer> keys = new ArrayList<Integer>();
			keys.addAll(keySet);
			Collections.shuffle(keys);
			for (int j=0; j<3; j++) {
				Integer key = keys.get(j);
				Integer value = tagMap.get(key);
				Tag tag = getById(key);
				if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
					tagMap.remove(key);
				} else {
					tagMap.put(key, value - Integer.valueOf(1));
				}
				newUser.addToFavouriteTags(tag);
			}
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for(int i=0; i<lonerNumber; i++) {
			Sex sex = null;
			if (random.nextInt(100) < 64) {
				sex = Sex.FEMALE;
			} else {
				sex = Sex.MALE;
			}
			User newUser = new User(space, grid, sex, UserCharacteristics.lonerCharacteristics());
			context.add(newUser);
			Set<Integer> keySet = tagMap.keySet();
			ArrayList<Integer> keys = new ArrayList<Integer>();
			keys.addAll(keySet);
			Collections.shuffle(keys);
			for (int j=0; j<3; j++) {
				Integer key = keys.get(j);
				Integer value = tagMap.get(key);
				Tag tag = getById(key);
				if (value - Integer.valueOf(1) == Integer.valueOf(0)) {
					tagMap.remove(key);
				} else {
					tagMap.put(key, value - Integer.valueOf(1));
				}
				newUser.addToFavouriteTags(tag);
			}
			usersMap.put(newUser.getUserId(), newUser);
		}
		
		for(Object obj : context) {
			NdPoint point = space.getLocation(obj);
			grid.moveTo(obj, (int)point.getX(), (int)point.getY());
		}

		mainContext = context;
		return context;
	}
	
	public Tag getById(int popularity) {
	    for(Tag e : Tag.values()) {
	        if(e.getValue() == popularity) return e;
	    }
	    return null;
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
