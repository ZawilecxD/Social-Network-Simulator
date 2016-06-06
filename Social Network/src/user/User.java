package user;

import org.apache.commons.collections.CollectionUtils;
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
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import network.Event;
import network.Group;
import network.SocialNetworkContext;
import network.Tag;
import network.TypeOfResult;

public class User {
	private final static Logger logger = Logger.getLogger(User.class);
	private ContinuousSpace <Object> space;
	private Grid<Object> grid;
//	private Context<Object> context;
	private @Getter int userId;
	private @Getter Sex sex;
	private @Getter boolean loggedIn;
	private @Getter PageRank pageRank;
	private @Getter ActivityRank activityRank;

	/**
	 * Cechy charakteru definiuj¹ce danego user'a i jego chêæ do wykonywania ró¿nych akcji
	 */
	private @Getter UserCharacteristics characteristics;
	
	/**
	 * Ustatalany ka¿dorazowo przy zalogowaniu do servisu, powiedzmy ¿e ma wartoœci miêdzy 0.0-10.0 
	 * gdzie 10.0 to super nastawienie i chêæ kontaktu z innymi userami
	 */
	private @Getter double mood; 
	private double sessionStartingMood;
	
	/**
	 * Collection of friends userIds
	 */
	private @Getter ArrayList<Integer> friends;
	
	/**
	 * Collection of events this user attends, eventId -> Event object
	 */
	private @Getter  ArrayList<Integer> events;
	
	private @Getter ArrayList<Tag> favouriteTags;
	
	private @Getter int currentSessionLength;
	private int nextDayStartTick;
	
	private @Getter ArrayList<Integer> relatives;
	
	private @Getter List<Integer> currentPostsIDs = new ArrayList<Integer>();
	private @Getter List<Integer> alreadySeenPostsIDs = new ArrayList<Integer>();
	private @Getter List<Integer> createdGroups = new ArrayList<Integer>();
	private @Getter List<Integer> createdEvents = new ArrayList<Integer>();
	
	private @Getter List<Integer> interestingGroupsIDs = new ArrayList<Integer>();
	private @Getter List<Integer> interestingEventsIDs = new ArrayList<Integer>();
	private @Getter List<Integer> joinedGroupsIDs = new ArrayList<Integer>();
	private @Getter List<Integer> joinedEventsIDs = new ArrayList<Integer>();
	
	public User(ContinuousSpace<Object> space, Grid<Object> grid, Sex sex, UserCharacteristics character) {
		this.space = space;
		this.grid = grid;
//		this.context = SocialNetworkContext.getMainContext();
		this.userId = SocialNetworkContext.getNextUserId();
		this.sex = sex;
		this.characteristics = character;
		this.pageRank = new PageRank();
		this.activityRank = new ActivityRank();
		this.friends = new ArrayList<Integer>();
		this.relatives = new ArrayList<Integer>();
		this.favouriteTags = new ArrayList<Tag>();
		this.events = new ArrayList<Integer>();
	}
	
	@ScheduledMethod(start = 1, interval = 1) //used to synchronize Repast Simphony to increment ticks by '1'
	public void checkSession() {
		if(loggedIn) {
			currentSessionLength --;
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
			
			checkMyActiveEventStatus();
			
			if(currentPostsIDs.isEmpty()) {
				collectInterestingPosts();
			} else {
				tryToComment();
				tryToLike();
			}
			tryToAnswerCommentInAlreadySeenPost();
			tryToPost();
			tryToFindAFriend();
			
			tryToCreateEvent();
			tryToCreateGroup();
			
			if(interestingGroupsIDs.isEmpty()) {
				collectInterestingGroups();
			} else if(!interestingGroupsIDs.isEmpty()){
				tryToJoinGroup();
			}
			
			if(interestingEventsIDs.isEmpty()) {
				collectInterestingGroups();
			} else if(!interestingGroupsIDs.isEmpty()){
				tryToJoinEvent();
			}
			
		} 
		
	}
	
	private void checkMyActiveEventStatus() {
		if(createdEvents.size() < 1) {
			return;
		}
		
		Event event = SocialNetworkContext.getEventById(createdEvents.get(createdEvents.size()-1));
		if(SocialNetworkContext.getCurrentTick() >= event.getEndTick() && event.isActive()) {
			event.finishThisEvent();
		}
	}
	
	public void logIn() {
		this.loggedIn = true;
		if(RandomHelper.nextIntFromTo(0, 100) <= characteristics.getDailyMood()) {
			mood = RandomHelper.nextDoubleFromTo(5.0, 10.0);
		} else {
			mood = RandomHelper.nextDoubleFromTo(0.0, 5.0);
		}
		sessionStartingMood = mood;
		collectInterestingPosts();
		collectInterestingGroups();
		collectInterestingEvents();
		calculateSessionLength();
		logger.debug(String.format("USER %s LOGGED IN, SESSION WILL LAST %d ticks", userId, currentSessionLength));
	}
	
	private void tryToFindAFriend() {
		
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getMeetNewFriendsRate() && isInMood()) {
			
			Comparator<User> byValueToUser = (user1, user2) -> Integer.compare(
		            user1.calculateValueToUser(this), user2.calculateValueToUser(this));
			
			 List<User> notKnownUsers = SocialNetworkContext.getUsersMap().values().stream()
			 	.filter(u -> !friends.contains(u.getUserId()) && !relatives.contains(u.getUserId()))
//			 	.filter(u -> !Collections.disjoint(friends, u.getFriends()) || !Collections.disjoint(joinedGroupsIDs, u.getJoinedGroupsIDs()))
			 	.filter(u -> isFriendshipPossible(u))
			 	.sorted(byValueToUser.reversed())
			 	.collect(Collectors.toList());
			 if(!notKnownUsers.isEmpty()) {
				User newFriend = notKnownUsers.get(0);
				
				if(RandomHelper.nextIntFromTo(0, 99) < SocialNetworkContext.CHANCE_TO_MEET_RELATIVES) {
					addRelative(newFriend);
				} else {
					addFriend(newFriend);
				}
			 }
		}
	}
	
	private boolean isFriendshipPossible(User user) {
		if(user.getUserId() == this.userId) {
			return false;
		}
		
		if(user.getFriends().size() < SocialNetworkContext.MINIMAL_FRIENDS_COUNT || this.friends.size() < SocialNetworkContext.MINIMAL_FRIENDS_COUNT) {
			return true;
		}
		
		if(!Collections.disjoint(friends, user.getFriends()) || !Collections.disjoint(relatives, user.getRelatives())) {
			int value = user.calculateValueToUser(this);
			if(value >= SocialNetworkContext.MINIMAL_VALUE_OF_FRIEND) {
				return true;
			}
		}
			
		
		return false;
	}
	
	public int calculateValueToUser(User userEvaluatingMe) {
		int numberOfCommonTags = CollectionUtils.intersection(favouriteTags, userEvaluatingMe.getFavouriteTags()).size();
		int numberOfCommonFriends = CollectionUtils.intersection(friends, userEvaluatingMe.getFriends()).size();
		int numberOfCommonRelatives = CollectionUtils.intersection(relatives, userEvaluatingMe.getRelatives()).size();
		int valueToUser = 20*numberOfCommonTags + 30*numberOfCommonFriends + 50*numberOfCommonRelatives;
		return valueToUser;
	}
	
	public boolean isInMood() {
		return RandomHelper.nextIntFromTo(0, 10) < mood;
	}
	
	public void tryToChat() {
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getChatRate() && isInMood()) {
			if(RandomHelper.nextIntFromTo(0, 99) < 60) {
				int randomIndex = RandomHelper.nextIntFromTo(0, relatives.size());
				int chosenRelativeId = relatives.get(randomIndex);
				User chatPartner = SocialNetworkContext.getUserById(chosenRelativeId);
				chatWithUser(chatPartner);
				chatPartner.chatWithUser(chatPartner);
			} else {
				int randomIndex = RandomHelper.nextIntFromTo(0, friends.size());
				int chosenRelativeId = friends.get(randomIndex);
				User chatPartner = SocialNetworkContext.getUserById(chosenRelativeId);
				chatWithUser(chatPartner);
				chatPartner.chatWithUser(chatPartner);
			}
		}
	}
	
	private void tryToJoinGroup() {
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getGroupBelongRate() && isInMood()) {
			int chosenGroupId = interestingGroupsIDs.get(0); //id of the most interesting group at this time
			Group chosenGroup = SocialNetworkContext.getGroupById(chosenGroupId);
			joinGroup(chosenGroup);
		}
	}
	
	private void tryToJoinEvent() {
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getEventParticipationRate() && isInMood()) {
			int chosenEventId = interestingEventsIDs.get(0); //id of the most interesting group at this time
			Event chosenEvent = SocialNetworkContext.getEventById(chosenEventId);
			joinEvent(chosenEvent);
		}
	}
	
	private void tryToPost() {
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getPostRate() && isInMood()) {
			createBoardPost();
		}
	}
	
	private void tryToComment() {
		if(!currentPostsIDs.isEmpty()){
			if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getCommentRate() && isInMood()) {
				int postId = currentPostsIDs.get(0);
				currentPostsIDs.remove(0);
				alreadySeenPostsIDs.add(postId);
				Post postToComment = SocialNetworkContext.getPostById(postId);
				
				commentPost(postToComment);
			}
		}
	}
	
	private void tryToAnswerCommentInAlreadySeenPost() {
		if(!alreadySeenPostsIDs.isEmpty()) {
			if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getCommentRate() && isInMood()) {
				int randomIndex = RandomHelper.nextIntFromTo(0, alreadySeenPostsIDs.size() -1);
				int postId = alreadySeenPostsIDs.get(randomIndex);
				Post postToComment = SocialNetworkContext.getPostById(postId);
				answerPostComment(postToComment);
			}
		}
	}
	
	private void tryToLike() {
		if(!currentPostsIDs.isEmpty()){
			if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getLikeRate() && isInMood()) {
				int postId = currentPostsIDs.get(0);
				currentPostsIDs.remove(0);
				alreadySeenPostsIDs.add(postId);
				Post postToLike = SocialNetworkContext.getPostById(postId);
				likePost(postToLike);
			}
		}
	}
	
	private void tryToCreateGroup() {
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getGroupHostingRate() && isInMood()
				&& this.createdGroups.size() <= SocialNetworkContext.MAX_GROUPS_PER_OWNER)  {
			createGroup();
		}
	}
	
	private void tryToCreateEvent() {
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getEventHostingRate() && isInMood())  {
			if(createdEvents.isEmpty()) {
				createEvent();
			} else {
				int lastEventId = createdEvents.get(createdEvents.size()-1);
				if(!SocialNetworkContext.getEventsMap().get(lastEventId).isActive()) {
					createEvent();
				}
			}
		}
	}
	
	private void calculateSessionLength() {
		currentSessionLength = characteristics.getAverageDailySessionLength();
		if(RandomHelper.nextIntFromTo(0, 1) == 0) {
			currentSessionLength -= RandomHelper.nextIntFromTo(0, 
					characteristics.getAverageDailySessionLength() - SocialNetworkContext.MINIMAL_SESSION_TIME);
		} else {
			currentSessionLength += RandomHelper.nextIntFromTo(0, SocialNetworkContext.DAY_LENGTH_IN_TICKS - characteristics.getAverageDailySessionLength());
		}
	}
	
	public void logOut() {
		logger.debug(String.format("USER %d LOGGED OUT", userId));
		calculateSessionResults();
		this.loggedIn = false;
	}
	
	private void calculateSessionResults() {
		if(mood < 0.7*sessionStartingMood) {
			this.characteristics.changeAverageMood(-1);
		} else if(sessionStartingMood < mood) {
			this.characteristics.changeAverageMood(1);
		}
	}
	
	private void collectInterestingPosts() {
		currentPostsIDs.clear();
		Comparator<Post> byValueToUser = (post1, post2) -> Integer.compare(
	            post1.calculateValueToUser(this), post2.calculateValueToUser(this));
		
		SocialNetworkContext.getPostsMap().values().stream()
				.filter(p -> (!currentPostsIDs.contains(p) && !alreadySeenPostsIDs.contains(p)))
				.filter(p -> !Collections.disjoint(p.getTags(), favouriteTags) || friends.contains(p.getOwnerID()))
				.filter(p -> !(p.getOwnerID() == this.userId))
				.limit(SocialNetworkContext.MAX_FOUND_POSTS)
				.sorted(byValueToUser.reversed())
				.forEach(p -> currentPostsIDs.add(p.getPostId()));
	}
	
	private void collectInterestingGroups() {
		this.interestingGroupsIDs.clear();
		Comparator<Group> byValueToUser = (group1, group2) -> Integer.compare(
				group1.calculateValueToUser(this), group2.calculateValueToUser(this));
		
		if(!SocialNetworkContext.getGroupsMap().isEmpty()) {
			//we collect groups that have interesting tags or group owner is our friend or group has our friends in it
			SocialNetworkContext.getGroupsMap().values().stream()
			.filter(g -> (!joinedGroupsIDs.contains(g)))
			.filter(g -> !createdGroups.contains(g.getGroupId()))
			.filter(g -> !Collections.disjoint(g.getTags(), favouriteTags) || friends.contains(g.getOwnerID())
					|| relatives.contains(g.getOwnerID()) || !Collections.disjoint(friends, g.getUsers()))
			.limit(SocialNetworkContext.MAX_FOUND_GROUPS)
			.sorted(byValueToUser.reversed())
			.forEach(g -> interestingGroupsIDs.add(g.getGroupId()));
		}
	}
	
	private void collectInterestingEvents() {
		this.interestingEventsIDs.clear();
		Comparator<Event> byValueToUser = (event1, event2) -> Integer.compare(
				event1.calculateValueToUser(this), event2.calculateValueToUser(this));
		
		if(!SocialNetworkContext.getEventsMap().isEmpty()) {
			//we collect events that have interesting tags or event owner is our friend or event has our friends in it
			SocialNetworkContext.getEventsMap().values().stream()
					.filter(e -> (!joinedEventsIDs.contains(e.getEventId())))
					.filter(e -> e.isActive())
					.filter(e -> !createdEvents.contains(e.getEventId()))
					.filter(e ->  friends.contains(e.getOwnerID()) || relatives.contains(e.getOwnerID()) || !Collections.disjoint(friends, e.getParticipantsIDs()))
					.limit(SocialNetworkContext.MAX_FOUND_EVENTS)
					.sorted(byValueToUser.reversed())
					.forEach(e -> interestingEventsIDs.add(e.getEventId()));
		}

	}
	
	
	public void addFriend(User newFriend){
		logger.debug(String.format("NEW FRIENDSHIP: %s AND %s", userId, newFriend.getUserId()));
		friends.add(newFriend.getUserId());
		newFriend.justAddFriendId(userId);
		Context<Object> context = ContextUtils.getContext(this);
		Network<Object> friendshipNetwork = (Network<Object>) context.getProjection("friendships network");
		friendshipNetwork.addEdge(this, newFriend);
	}
	
	public void addRelative(User newRelative){
		logger.debug(String.format("NEW FRIENDSHIP(Relatives): %s AND %s", userId, newRelative.getUserId()));
		relatives.add(newRelative.getUserId());
		newRelative.justAddRelative(userId);
		Context<Object> context = ContextUtils.getContext(this);
		Network<Object> friendshipNetwork = (Network<Object>) context.getProjection("friendships network");
		friendshipNetwork.addEdge(this, newRelative);
	}
	
	public void justAddFriendId(int id) {
		friends.add(id);
	}
	
	public void justAddRelative(int id) {
		relatives.add(id);
	}
	
	public void createBoardPost(){
		Post newPost = new Post(userId);
		int startingPopularity = (int) Math.floor(pageRank.getPoints()/100);
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
		activityRank.createdPost();
		logger.debug(String.format("USER %s CREATED BOARD POST %s", userId, newPost.getPostId()));
	}
	
	public void createGroupPost(List<Tag> tagsToAdd) {
		Post newPost = new Post(userId);
		int startingPopularity = (int) Math.floor(pageRank.getPoints()/100);
		newPost.setPopularity(startingPopularity);
		newPost.addTags(tagsToAdd.toArray(new Tag[tagsToAdd.size()]));
		SocialNetworkContext.addPost(newPost);
		activityRank.createdPost();
		logger.debug(String.format("USER %s CREATED GROUP POST %s", userId, newPost.getPostId()));
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
		if(!post.getCommentersIDs().isEmpty()) {
			int randomIndex = RandomHelper.nextIntFromTo(0, post.getCommentersIDs().size()-1);
			int targettedCommentAuthor = post.getCommentersIDs().get(randomIndex);
			TypeOfResult commentResult = calculateResult();
			post.answerToComment(userId, targettedCommentAuthor, commentResult);
			activityRank.gaveComment(commentResult);
			logger.debug(String.format("USER %s ANSWERED COMMENT OF USER %s", userId, targettedCommentAuthor));
		}
	}
	
	public void commentPost(Post post){
		TypeOfResult commentResult = calculateResult();
		post.commentIt(userId, commentResult);
		activityRank.gaveComment(commentResult);
		logger.debug(String.format("USER %s COMMENTED POST  %s", userId, post.getPostId()));
	}
	
	public void chatWithUser(User chatPartner) {
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getAssertiveness()) {
			List<Tag> tagsToGain = chatPartner.getFavouriteTags().stream()
					.filter(t -> !favouriteTags.contains(t)).collect(Collectors.toList());
			if(!tagsToGain.isEmpty()) {
				int randomIndex = RandomHelper.nextIntFromTo(0, tagsToGain.size() - 1);
				Tag tagToGain = tagsToGain.get(randomIndex);
				addToFavouriteTags(tagToGain);
			}
		}
		
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getAssertiveness()) {
			List<Integer> groupsToGain = chatPartner.getJoinedGroupsIDs().stream()
					.filter(g -> !joinedGroupsIDs.contains(g)).collect(Collectors.toList());
			if(!groupsToGain.isEmpty()) {
				int randomIndex = RandomHelper.nextIntFromTo(0, groupsToGain.size() - 1);
				Group groupToGain =SocialNetworkContext.getGroupById(groupsToGain.get(randomIndex)) ;
				joinGroup(groupToGain);
			}
		}
		

		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getAssertiveness()) {
			List<Integer> eventsToGain = chatPartner.getJoinedEventsIDs().stream()
					.filter(e -> !joinedEventsIDs.contains(e)).collect(Collectors.toList());
			if(!eventsToGain.isEmpty()) {
				int randomIndex = RandomHelper.nextIntFromTo(0, eventsToGain.size() - 1);
				Event eventToGain = SocialNetworkContext.getEventById(eventsToGain.get(randomIndex)) ;
				joinEvent(eventToGain);
			}
		}
		
		this.changeMoodByChatResult(chatPartner, chatResultForUser());		
	}
	
	private TypeOfResult chatResultForUser() {
		int randomResult = RandomHelper.nextIntFromTo(1, 3);
		TypeOfResult chatResult = TypeOfResult.NEUTRAL;
		switch(randomResult) {
		case 1:
			chatResult = TypeOfResult.POSITIVE;
			break;
		case 2:
			chatResult = TypeOfResult.NEGATIVE;
			break;
		}
		
		return chatResult;
	}
		
	
	public void likePost(Post post){
		post.likeIt(getPageRankPoints());
		activityRank.gaveLike();
		logger.debug(String.format("USER %s LIKED POST  %s", userId, post.getPostId()));
	}
	
	public void changeMoodByEvent(int eventId, TypeOfResult eventResult, boolean isEventMine) {
		Event event = SocialNetworkContext.getEventById(eventId);
		switch(eventResult) {
		case NEGATIVE:
			eventMoodChange(event.calculateValueToUser(this), -1, isEventMine);
			break;
		case POSITIVE:
			eventMoodChange(event.calculateValueToUser(this), 1, isEventMine);
			break; 
		}
	}
	
	public void changeMoodByChatResult(User chatPartner, TypeOfResult chatResult) {
		switch(chatResult) {
		case NEGATIVE:
			chatMoodChange(calculateValueToUser(chatPartner), chatPartner.calculateValueToUser(this), -1);
			break;
		case NEUTRAL:
			chatMoodChange(calculateValueToUser(chatPartner), chatPartner.calculateValueToUser(this), 1/3);
			break;
		case POSITIVE:
			chatMoodChange(calculateValueToUser(chatPartner), chatPartner.calculateValueToUser(this), 1);
			break; 
		}
	}
	
	public void changeMoodByAnswerToComment(int answerAuthorId, TypeOfResult answerResult, boolean isMyPost) {
		User authorOfTheAnswer = SocialNetworkContext.getUserById(answerAuthorId);
        double differenceInPageRanks = authorOfTheAnswer.getPageRank().getPoints() - pageRank.getPoints();
        double usersAttitude = 1;
        if(relatives.contains(answerAuthorId)) {
        	usersAttitude = 2;
        } else if(friends.contains(answerAuthorId)) {
        	usersAttitude = 1.5;
        }
		switch(answerResult) {
		case NEGATIVE:
			//negative answer means worse mood
			answerMoodChange(differenceInPageRanks, -1, usersAttitude, isMyPost);
			break;
		case NEUTRAL:
			//somebody answered so we get some pageRank (3 times less than with POSITIVE scenario)
			answerMoodChange(differenceInPageRanks, 1/3, usersAttitude, isMyPost);
			break;
		case POSITIVE:
			//we got positive answer so our mood profits
			answerMoodChange(differenceInPageRanks, 1, usersAttitude, isMyPost);
			break; 
		}
	}
	
	private void answerMoodChange(double differenceInPageRanks, double resultModifier, double attitude, boolean isMyPost) {
		if(differenceInPageRanks > 0) {
			mood += (Math.ceil(differenceInPageRanks/(pageRank.getPoints()*10))) * resultModifier * attitude;
		}else {
			mood += 0.1 * attitude * resultModifier;
		}
		
		if(isMyPost) {
			if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getVulnerability()) {
				characteristics.changePostRate((int) resultModifier);
			}
		} else {
			if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getVulnerability()) {
				characteristics.changeCommentRate((int) resultModifier);
			}
		}
		
		
		if(mood > 10.0) {
			mood = 10.0;
		}
		
		if(mood < 0) {
			mood = 0;
		}
	}
	
	private void chatMoodChange(int valueOfChatPartner, int ourValueToChatPartner, double resultModifier) {
		mood += (Math.ceil(valueOfChatPartner/ourValueToChatPartner*10)) * resultModifier;
		
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getVulnerability()) {
			characteristics.changeChatRate((int) resultModifier);
		}
		
		if(mood > 10.0) {
			mood = 10.0;
		}
		
		if(mood < 0) {
			mood = 0;
		}
	}
	
	private void eventMoodChange(int valueOfEventToUser, double resultModifier, boolean isEventMine) {
		int value = (int) (Math.ceil(valueOfEventToUser/1000));
		if(value < 1) value = 1;
		
		mood += value * resultModifier;
		
		if(isEventMine) {
			if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getVulnerability()) {
				characteristics.changeEventHostingRate((int) resultModifier);
			}
		} else {
			if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getVulnerability()) {
				characteristics.changeEventParticipationRate((int) resultModifier);
			}
		}
		
		
		if(mood > 10.0) {
			mood = 10.0;
		}
		
		if(mood < 0) {
			mood = 0;
		}
	}

	public void createGroup(){
		Group createdGroup = new Group(userId);
		List<Tag> tagsToAdd = new ArrayList<>();
		
		for(Tag tag : this.getFavouriteTags()) {
			if(RandomHelper.nextIntFromTo(1,2) == 1) {
				tagsToAdd.add(tag);
			}
		}
			
		if(tagsToAdd.isEmpty()) {
			int randomIndex = RandomHelper.nextIntFromTo(0, favouriteTags.size()-1);
			tagsToAdd.add(favouriteTags.get(randomIndex));
		}
		
		Tag[] tags = new Tag[tagsToAdd.size()];
		tagsToAdd.toArray(tags);
		int startingPopularity = (int) Math.ceil(pageRank.getPoints()/10);
		createdGroup.setStartingPopularity(startingPopularity);
		createdGroup.addTags(tags);
		
		SocialNetworkContext.addGroup(createdGroup);
		this.createdGroups.add(createdGroup.getGroupId());
		this.activityRank.createdGroup();
	}
	
	public void createEvent(){
		Event createdEvent = new Event(userId);
		List<Tag> tagsToAdd = new ArrayList<Tag>();
		
		for(Tag tag : this.getFavouriteTags()) {
			if(RandomHelper.nextIntFromTo(1,2) == 1) {
				tagsToAdd.add(tag);
			}
		}
			
		if(tagsToAdd.isEmpty()) {
			int randomIndex = RandomHelper.nextIntFromTo(0, favouriteTags.size()-1);
			tagsToAdd.add(favouriteTags.get(randomIndex));
		}
		
		Tag[] tags = new Tag[tagsToAdd.size()];
		tagsToAdd.toArray(tags);
		
		int startingPopularity = (int) Math.ceil(pageRank.getPoints()/10);
		createdEvent.setStartingPopularity(startingPopularity);
		createdEvent.addTags(tags);
		
		SocialNetworkContext.addEvent(createdEvent);
		this.createdEvents.add(createdEvent.getEventId());
		this.activityRank.createdEvent();
	}
	
	public void joinGroup(Group group){
		if(!joinedGroupsIDs.contains(group.getGroupId())) {
			group.addUser(userId);
			joinedGroupsIDs.add(group.getGroupId());
		}
		
	}
	
	public void joinEvent(Event event){
		if(!joinedEventsIDs.contains(event.getEventId()) && event.isActive()) {
			event.addUser(userId);
			joinedEventsIDs.add(event.getEventId());
		}
	}
	
	public void increasePageRank(int anotherUserPageRank) {
		pageRank.addPoints(anotherUserPageRank);
	}
	
	public void increaseActivityRank(int value) {
		activityRank.addPoints(value);
	}
	
	//getters of ActivityRank fields used to show info about user in simulator agent view ----------------------------------------------------
	public int getActivityRankPoints() {
		return activityRank.getPoints();
	}
	
	public int getActivityRankCommentsGiven() {
		return activityRank.getCommentsGiven();
	}
	
	public int getActivityRankNegetiveCommentsGiven() {
		return activityRank.getNegativCommentsGiven();
	}
	
	public int getActivityRankEventsCreated() {
		return activityRank.getEventsCreated();
	}
	
	public int getActivityRankGroupsCreated() {
		return activityRank.getGroupsCreated();
	}
	
	public int getActivityRankLikesGiven() {
		return activityRank.getLikesGiven();
	}
	
	public int getActivityRankSuccessfulEvents() {
		return activityRank.getSuccessfulEvents();
	}
	
	public int getActivityRankPostsCreated() {
		return activityRank.getPostsCreated();
	}

	//getters of PageRank fields used to show info about user in simulator agent view ----------------------------------------------------
	public int getPageRankPoints() {
		return pageRank.getPoints();
	}
	
	public int getPageRankReceivedComments() {
		return pageRank.getReceivedComments();
	}

	public int getPageRankReceivedEventCompetitors() {
		return pageRank.getReceivedEventCompetitors();
	}
	
	public int getPageRankReceivedGroupsMembers() {
		return pageRank.getReceivedGroupsMembers();
	}
	
	public int getPageRankReceivedLikes() {
		return pageRank.getReceivedLikes();
	}
	
	public int getPageRankReceivedNegetiveComments() {
		return pageRank.getReceivedNegetiveComments();
	}
	
}