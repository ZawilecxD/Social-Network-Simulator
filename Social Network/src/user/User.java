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
	
	private @Getter List<Integer> interestingGroupsIDs = new ArrayList<Integer>();
	private @Getter List<Integer> joinedGroupsIDs = new ArrayList<Integer>();
	
	public User(Context<Object> context, Sex sex, UserCharacteristics character) {
		this.context = context;
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
	
	@ScheduledMethod(start = 1, interval = 1) //used only to synchronize Repast Simphony to increment ticks by '1'
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
			if(currentPostsIDs.isEmpty()) {
				collectInterestingPosts();
			} else {
				tryToComment();
				tryToLike();
			}
			tryToAnswerCommentInAlreadySeenPost();
			tryToPost();
			tryToFindAFriend();
			
			if(interestingGroupsIDs.isEmpty()) {
				collectInterestingGroups();
			} else {
				tryToJoinGroup();
			}
			
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
		logger.debug(String.format("USER %s LOGGED IN, SESSION WILL LAST %d ticks", userId, currentSessionLength));
	}
	
	private void tryToFindAFriend() {
		if(RandomHelper.nextIntFromTo(1, 100) < characteristics.getMeetNewFriendsRate() && isInMood()) {
			
			Comparator<User> byValueToUser = (user1, user2) -> Integer.compare(
		            user1.calculateValueToUser(this), user2.calculateValueToUser(this));
			
			 List<User> notKnownUsers = SocialNetworkContext.getUsersMap().values().stream()
			 	.filter(u -> !friends.contains(u.getUserId()))
			 	.filter(u -> !Collections.disjoint(friends, u.getFriends()) || !Collections.disjoint(joinedGroupsIDs, u.getJoinedGroupsIDs()))
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
	
	public int calculateValueToUser(User userEvaluatingMe) {
		int numberOfCommonTags = CollectionUtils.intersection(favouriteTags, userEvaluatingMe.getFavouriteTags()).size();
		int numberOfCommonFriends = CollectionUtils.intersection(friends, userEvaluatingMe.getFriends()).size();
		int numberOfCommonRelatives = CollectionUtils.intersection(relatives, userEvaluatingMe.getRelatives()).size();
		int valueToUser = 20*numberOfCommonTags + 30*numberOfCommonFriends + 50*numberOfCommonRelatives;
		return valueToUser;
	}
	
	private boolean isInMood() {
		return RandomHelper.nextIntFromTo(0, 10) < mood;
	}
	
	public void tryToChat() {
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getChatRate() && isInMood()) {
			
		}
	}
	
	private void tryToJoinGroup() {
		if(RandomHelper.nextIntFromTo(0, 99) < characteristics.getGroupBelongRate() && isInMood()) {
			int chosenGroupId = interestingGroupsIDs.get(0); //id of the most interesting group at this time
			Group chosenGroup = SocialNetworkContext.getGroupById(chosenGroupId);
			joinGroup(chosenGroup);
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
				currentPostsIDs.remove(postId);
				alreadySeenPostsIDs.add(postId);
				Post postToLike = SocialNetworkContext.getPostById(postId);
				likePost(postToLike);
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
		this.loggedIn = false;
	}
	
	private void collectInterestingPosts() {
		currentPostsIDs.clear();
		Comparator<Post> byValueToUser = (post1, post2) -> Integer.compare(
	            post1.calculateValueToUser(this), post2.calculateValueToUser(this));
		
		SocialNetworkContext.getPostsMap().values().stream()
				.filter(p -> (!currentPostsIDs.contains(p) && !alreadySeenPostsIDs.contains(p)))
				.filter(p -> !Collections.disjoint(p.getTags(), favouriteTags) || friends.contains(p.getOwnerID()))
				.filter(p -> !(p.getOwnerID() == this.userId))
				.sorted(byValueToUser.reversed())
				.forEach(p -> currentPostsIDs.add(p.getPostId()));
		logger.debug("TRIED AND GOT "+currentPostsIDs.size()+" POSTS!!!");
	}
	
	private void collectInterestingGroups() {
		Comparator<Group> byValueToUser = (group1, group2) -> Integer.compare(
				group1.calculateValueToUser(this), group2.calculateValueToUser(this));
		
		//we collect groups that have interesting tags or groupd owner is our friend or group has our friends in it
		SocialNetworkContext.getGroupsMap().values().stream()
				.filter(g -> (!joinedGroupsIDs.contains(g)))
				.filter(g -> !Collections.disjoint(g.getTags(), favouriteTags) || friends.contains(g.ownerID) || !Collections.disjoint(friends, g.users))
				.sorted(byValueToUser.reversed())
				.forEach(g -> interestingGroupsIDs.add(g.getGroupId()));
	}
	
	
	public void addFriend(User newFriend){
		logger.debug(String.format("NEW FRIENDSHIP: %s AND %s", userId, newFriend.getUserId()));
		friends.add(newFriend.getUserId());
		newFriend.justAddFriendId(userId);
		Network<Object> friendshipNetwork = (Network<Object>) context.getProjection("friendships network");
		friendshipNetwork.addEdge(this, newFriend);
	}
	
	public void addRelative(User newRelative){
		logger.debug(String.format("NEW FRIENDSHIP(Relatives): %s AND %s", userId, newRelative.getUserId()));
		relatives.add(newRelative.getUserId());
		newRelative.justAddRelative(userId);
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
		int randomIndex = RandomHelper.nextIntFromTo(0, post.getCommentersIDs().size()-1);
		int targettedCommentAuthor = post.getCommentersIDs().get(randomIndex);
		TypeOfResult commentResult = calculateResult();
		post.answerToComment(userId, targettedCommentAuthor, commentResult);
		activityRank.gaveComment(commentResult);
		logger.debug(String.format("USER %s ANSWERED COMMENT OF USER %s", userId, targettedCommentAuthor));
	}
	
	public void commentPost(Post post){
		TypeOfResult commentResult = calculateResult();
		post.commentIt(userId, commentResult);
		activityRank.gaveComment(commentResult);
		logger.debug(String.format("USER %s COMMENTED POST  %s", userId, post.getPostId()));
	}
	
	public void likePost(Post post){
		post.likeIt(getPageRankPoints());
		activityRank.gaveLike();
		logger.debug(String.format("USER %s LIKED POST  %s", userId, post.getPostId()));
	}
	
	public void changeMoodByAnswerToComment(int answerAuthorId, TypeOfResult answerResult) {
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
			answerMoodChange(differenceInPageRanks, -1, usersAttitude);
			break;
		case NEUTRAL:
			//somebody answered so we get some pageRank (3 times less than with POSITIVE scenario)
			answerMoodChange(differenceInPageRanks, 1/3, usersAttitude);
			break;
		case POSITIVE:
			//we got positive answer so our mood profits
			answerMoodChange(differenceInPageRanks, 1, usersAttitude);
			break; 
		}
	}
	
	private void answerMoodChange(double differenceInPageRanks, double resultModifier, double attitude) {
		if(differenceInPageRanks > 0) {
			mood += (Math.ceil(differenceInPageRanks/(pageRank.getPoints()*10))) * resultModifier * attitude;
		}else {
			mood += 0.1 * attitude * resultModifier;
		}
		if(mood > 10.0) {
			mood = 10.0;
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
		group.addUser(userId);
		joinedGroupsIDs.add(group.getGroupId());
	}
	
	public void joinEvent(Event event){
		
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