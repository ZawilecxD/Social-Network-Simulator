package user;

import org.apache.log4j.Logger;

import posts.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import network.Event;
import network.Group;
import network.SocialNetworkContext;
import network.Tag;
import network.TypeOfResult;

public class User {
	private final static Logger logger = Logger.getLogger(User.class);
	private Context<Object> context;
	private @Getter int userId;
	private @Getter String name;
	private @Getter String surname;
	private @Getter Sex sex;
	private @Getter boolean loggedIn;
	private @Getter PageRank pageRank;
	private @Getter ActivityRank;

	/**
	 * Cechy charakteru definiuj¹ce danego user'a i jego chêæ do wykonywania ró¿nych akcji
	 */
	private @Getter UserCharacteristics characteristics;
	
	/**
	 * Ustatalany ka¿dorazowo przy zalogowaniu do servisu, powiedzmy ¿e ma wartoœci miêdzy 0.0-10.0 
	 * gdzie 10.0 to super nastawienie i chêæ kontaktu z innymi userami
	 */
	private @Getter double mood; 
	
	/**
	 * Collection of friends, userId -> User object
	 */
	private @Getter ArrayList<Integer> friends;
	
	/**
	 * Collection of groups this user is part of, groupId -> Group object
	 */
	private @Getter HashMap<Integer, Group> groups;
	
	/**
	 * Collection of events this user attends, eventId -> Event object
	 */
	private @Getter HashMap<Integer, Event> events;
	
	private @Getter ArrayList<Tag> favouriteTags;
	
	private @Getter int currentSessionLength;
	private int nextDayStartTick;
	
	private @Getter HashMap<Integer, User> relatives;
	
	private @Getter List<Integer> currentPostsIDs = new ArrayList<Integer>();
	private @Getter List<Integer> alreadySeenPostsIDs = new ArrayList<Integer>();
	
	private @Getter List<Integer> interestingGroupsIDs = new ArrayList<Integer>();
	private @Getter List<Integer> joinedGroupsIDs = new ArrayList<Integer>();
	
	public User(Context<Object> context, String name, String surname, Sex sex, UserCharacteristics character) {
		this.context = context;
		this.userId = SocialNetworkContext.getNextUserId();
		this.name = name;
		this.surname = surname;
		this.sex = sex;
		this.characteristics = character;
		this.pageRank = new PageRank();
		this.activityRank = new ActivityRank();
		
	}
	
	@ScheduledMethod(start = 1, interval = 1) //used only to synchronize Repast Simphony to increment ticks by '1'
	public void checkSession() {
		currentSessionLength --;
		if(loggedIn) {
			if(currentSessionLength <= 0) {
				logOut();
				nextDayStartTick += SocialNetworkContext.DAY_LENGTH_IN_TICKS;
				return;
			} 
		} else if(SocialNetworkContext.getCurrentTick() >= nextDayStartTick){
			logIn();
			return;
		}
	}
	
	@ScheduledMethod(start = 1, interval = 10) //action every 10minutes
	public void sessionActions() {
		if(loggedIn) {
			if(currentPostsIDs.isEmpty()) {
				collectInterestingPosts();
			}
			tryToFindAFriend();
			tryToPost();
			tryToComment();
			tryToLike();
			logger.debug("doing sth "+currentSessionLength);
		} 
		
	}
	
	public void logIn() {
		this.loggedIn = true;
		if(RandomHelper.nextIntFromTo(0, 100) <= characteristics.getDailyMood()) {
			mood = RandomHelper.nextDoubleFromTo(5.0, 10.0);
		} else {
			mood = RandomHelper.nextDoubleFromTo(0.0, 5.0);
		}
		collectInterestingPosts();
//		collectInterestingGroups();
		calculateSessionLength();
		logger.debug(String.format("USER LOGGED IN: %s, SESSION WILL LAST %d ticks", userId, currentSessionLength));
	}
	
	private void tryToFindAFriend() {
		if(RandomHelper.nextIntFromTo(1, 100) < characteristics.getMeetNewFriendsRate()) {
			 List<Integer> notKnownUsers = SocialNetworkContext.getUsersMap().keySet().stream()
			 	.filter(u -> !friends.contains(u)).collect(Collectors.toList());
			int randomIndex = RandomHelper.nextIntFromTo(0, notKnownUsers.size()-1);
			int randomUserId = notKnownUsers.get(randomIndex);
			User newFriend = SocialNetworkContext.getUserById(randomUserId);
			
			if(RandomHelper.nextIntFromTo(0, 99) < SocialNetworkContext.CHANCE_TO_MEET_RELATIVES) {
				addRelative(newFriend);
				logger.debug(String.format("USER %d ADDED %d AS RELATIVE"));
			} else {
				addFriend(newFriend);
				logger.debug(String.format("USER %d ADDED %d AS FRIEND"));
			}
			
		}
	}
	
	private void tryToPost() {
		if(RandomHelper.nextIntFromTo(1, 100) < characteristics.getPostRate()) {
			createBoardPost();
		}
	}
	
	private void tryToComment() {
		if(RandomHelper.nextIntFromTo(1, 100) < characteristics.getCommentRate()) {
			int randomIndex = RandomHelper.nextIntFromTo(0, currentPostsIDs.size()-1);
			int randomPostId = currentPostsIDs.get(randomIndex);
			currentPostsIDs.remove(randomPostId);
			alreadySeenPostsIDs.add(randomPostId);
			Post postToComment = SocialNetworkContext.getPostById(randomPostId);
			commentPost(postToComment);
		}
	}
	
	private void tryToLike() {
		if(RandomHelper.nextIntFromTo(1, 100) < characteristics.getLikeRate()) {
			int randomIndex = RandomHelper.nextIntFromTo(0, currentPostsIDs.size()-1);
			int randomPostId = currentPostsIDs.get(randomIndex);
			currentPostsIDs.remove(randomPostId);
			alreadySeenPostsIDs.add(randomPostId);
			Post postToLike = SocialNetworkContext.getPostById(randomPostId);
			likePost(postToLike);
		}
	}
	
	private void calculateSessionLength() {
		currentSessionLength = characteristics.getAverageDailySessionLength();
		if(RandomHelper.nextIntFromTo(1, 2) == 1) {
			currentSessionLength -= RandomHelper.nextIntFromTo(0, 
					characteristics.getAverageDailySessionLength() - SocialNetworkContext.MINIMAL_SESSION_TIME);
		} else {
			currentSessionLength += RandomHelper.nextIntFromTo(0, characteristics.getAverageDailySessionLength());
		}
	}
	
	public void logOut() {
		logger.debug(String.format("USER LOGGED OUT: %d", userId));
		this.loggedIn = false;
	}
	
	private void collectInterestingPosts() {
		Comparator<Post> byValueToUser = (post1, post2) -> Integer.compare(
	            post1.calculateValueToUser(this), post2.calculateValueToUser(this));
		
		SocialNetworkContext.getPostsMap().values().stream()
				.filter(p -> (!currentPostsIDs.contains(p) || !alreadySeenPostsIDs.contains(p)))
				.filter(p -> !Collections.disjoint(p.getTags(), favouriteTags))
				.filter(p -> !(p.getOwnerID() == this.userId))
				.sorted(byValueToUser.reversed())
				.forEach(p -> currentPostsIDs.add(p.getPostId()));
		
	}
	
	private void collectInterestingGroups() {
		Comparator<Group> byValueToUser = (group1, group2) -> Integer.compare(
				group1.calculateValueToUser(this), group2.calculateValueToUser(this));
		
		SocialNetworkContext.getGroupsMap().values().stream()
				.filter(g -> (!groups.containsKey(g)))
				.filter(g -> !Collections.disjoint(g.getTags(), favouriteTags))
				.sorted(byValueToUser.reversed())
				.forEach(g -> interestingGroupsIDs.add(g.getGroupId()));
	}
	
	
	public void addFriend(User newFriend){
		logger.debug(String.format("NEW FRIENDSHIP: %s AND %s", userId, newFriend.getUserId()));
		friends.add(newFriend.getUserId());
		Network<Object> friendshipNetwork = (Network<Object>) context.getProjection("");
		friendshipNetwork.addEdge(this, newFriend);
	}
	
	public void createBoardPost(){
		Post newPost = new Post(userId);
		int startingPopularity = (int) Math.floor(pageRank/100);
		newPost.setPopularity(startingPopularity);
		
		int numberOfTagsToAdd = RandomHelper.nextIntFromTo(1, 3);
		List<Tag> listCopy = new ArrayList<Tag>(favouriteTags);
		Tag[] tagsToAdd = new Tag[numberOfTagsToAdd];
		for(int i=0; i< numberOfTagsToAdd; i++) {
			Collections.shuffle(listCopy);
			tagsToAdd[i] = listCopy.get(0);
		}
		newPost.addTags(tagsToAdd);
		
		SocialNetworkContext.addPost(newPost);
		//TODO: add activityRank
	}
	
	public void createGroupPost(List<Tag> tagsToAdd) {
		Post newPost = new Post(userId);
		int startingPopularity = (int) Math.floor(pageRank/100);
		newPost.setPopularity(startingPopularity);
		newPost.addTags(tagsToAdd.toArray(new Tag[tagsToAdd.size()]));
		SocialNetworkContext.addPost(newPost);
		//TODO: add activityRank
	}

	public void addToFavouriteTags(Tag tag) {
		favouriteTags.add(tag);
	}
	
	private TypeOfResult calculateResult() {
		TypeOfResult resultType = TypeOfResult.NEUTRAL;
		
		//set result of the answer positive or negative if mood has given values
		if(mood >= 6) {
			resultType = TypeOfResult.POSITIVE;
		} else if (mood <= 3) {
			resultType = TypeOfResult.NEGATIVE;
		}	
		
		//if this person is a troll and likes making chaotic and negative comments/answers
		// then we change result to negative
		if(RandomHelper.nextIntFromTo(0, 100) < characteristics.getRageRate()) {
			resultType = TypeOfResult.NEGATIVE;
		}
		
		return resultType;
	}
	
	private void answerPostComment(Post post) {
		int targettedCommentAuthor = RandomHelper.nextIntFromTo(0, post.getCommentersIDs().size()-1);
		post.answerToComment(userId, targettedCommentAuthor, calculateResult());
		//TODO: add activityRank
	}
	
	public void commentPost(Post post){
		post.commentIt(userId);
		//TODO: add activityRank
	}
	
	public void likePost(Post post){
		post.likeIt();
		//TODO: add activityRank
	}
	
	public void changeMoodByAnswerToComment(int answerAuthorId, TypeOfResult answerResult) {
		User authorOfTheAnswer = SocialNetworkContext.getUserById(answerAuthorId);
        double differenceInPageRanks = authorOfTheAnswer.getPageRank() - pageRank;
		switch(answerResult) {
		case NEGATIVE:
			//negative answer means worse mood
			negativeAnswerMoodChange(differenceInPageRanks);
			break;
		case NEUTRAL:
			//somebody answered so we get some pageRank (3 times less than with POSITIVE scenario)
			positiveAnswerMoodChange(differenceInPageRanks, 3);
			break;
		case POSITIVE:
			//we got positive answer so our mood profits
			positiveAnswerMoodChange(differenceInPageRanks, 1);
			break; 
		}
	}
	
	private void positiveAnswerMoodChange(double differenceInPageRanks, int neutralModifier) {
		if(differenceInPageRanks > 0) {
			mood += (Math.ceil(differenceInPageRanks/(pageRank*10)) * 0.1)/neutralModifier;
		}else {
			mood += 0.1;
		}
		if(mood > 10.0) {
			mood = 10.0;
		}
	}
	
	private void negativeAnswerMoodChange(double differenceInPageRanks) {
		if(differenceInPageRanks > 0) {
			mood -= Math.ceil(differenceInPageRanks/(pageRank*10)) * 0.1;
		}else {
			mood -= 0.1;
		}
		if(mood < 0) {
			mood = 0;
		}
	}
	
	public void removeFriend(){
		
	}
	
	public void chatWithFriend(){
		
	}

	public void createGroup(){
		
	}
	
	public void createEvent(){
		
	}
	
	public void joinGroup(Group group){
		
	}
	
	public void joinEvent(Event event){
		
	}
	
	public void startelationship(){
		
	}
	
	public void addRelative(User relative) {
		this.relatives.put(relative.getUserId(), relative);
	}

	public void increasePageRank(int value) {
		pageRank += value;
	}
	

}