package network;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import lombok.Getter;
import lombok.Setter;
import user.User;

public class Group {
	
	public @Getter int groupId;
	public @Getter List<Integer> users; 
	public @Getter int  ownerID;
	public @Getter @Setter int popularity;
	public @Getter ArrayList<Tag> tags;

	public Group(int ownerId) {
		this.groupId = SocialNetworkContext.getNextGroupId();
		this.ownerID = ownerId;
		this.popularity = 0;
		this.users = new ArrayList<Integer>();
		this.tags = new ArrayList<Tag>();
	}
	
	@ScheduledMethod(start = 1, interval = 10)
	private void groupActions() {
		int chosenUserId = RandomHelper.nextIntFromTo(0, users.size()-1);
		tryUserToMakeGroupPost(chosenUserId);
	}
	
	private void tryUserToMakeGroupPost(int userId) {
		User chosenUser = SocialNetworkContext.getUserById(userId);
		List<Tag> tagsCopy = new ArrayList<Tag>(tags);
		tagsCopy.retainAll(chosenUser.getFavouriteTags());
		chosenUser.createGroupPost(tagsCopy);
	}
	
	public int calculateValueToUser(User user) {
		int numberOfCommonTags = CollectionUtils.intersection(tags, user.getFavouriteTags()).size();
		int valueToUser = popularity + 50*numberOfCommonTags;
		
		int numberOfFriendsInGroup = CollectionUtils.intersection(users, user.getFriends()).size();
		valueToUser += 10*numberOfFriendsInGroup;
		
		int numberOfRelativesInGroup = CollectionUtils.intersection(users, user.getRelatives()).size();
		valueToUser += 15*numberOfRelativesInGroup;
		
		return valueToUser;
	}
	
	public void addUser(int userId) {
		users.add(userId);
		increasePopularity(userId);
	}
	
	private void increasePopularity(int userId) {
		User newMember = SocialNetworkContext.getUserById(userId);
		popularity += newMember.getPageRankPoints()/10;
	}
	
	public void addTags(Tag... tags) {
		for(Tag tag : tags) {
			this.tags.add(tag);
		}
	}
	
}