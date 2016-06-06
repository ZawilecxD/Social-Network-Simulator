package network;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import lombok.Getter;
import user.User;

public class Event {

	private @Getter int eventId;
	private @Getter List<Integer> participantsIDs;
	private @Getter List<Tag> tags;
	private @Getter int popularity;
	private @Getter Integer ownerID;
	private @Getter boolean active;
	private @Getter int endTick; //at what tick does event ends!
	
	
	public Event(int ownerId) {
		this.eventId = SocialNetworkContext.getNextEventId();
		this.ownerID = ownerId;
		this.active = true;
		this.participantsIDs = new ArrayList<Integer>();
		this.tags = new ArrayList<Tag>();
		this.popularity = 0;
		
		//event will last random number of days 
		this.endTick = SocialNetworkContext.getCurrentTick() 
				+ (RandomHelper.nextIntFromTo(1, SocialNetworkContext.MAX_EVENT_TIME) * SocialNetworkContext.DAY_LENGTH_IN_TICKS); 
		System.out.println(eventId+ " konczy sie o  " + endTick);
	}
	
	public void finishThisEvent() {
		System.out.println("finiszuje "+eventId+" w ticku nr "+SocialNetworkContext.getCurrentTick() );
		this.active = false;
		TypeOfResult eventResult = calculateEventResult();
		User owner = SocialNetworkContext.getUserById(ownerID);
		if(eventResult == TypeOfResult.POSITIVE) {
			owner.getPageRank().eventSucceeded(popularity);
		} else {
			owner.changeMoodByEvent(eventId, eventResult, true);
		}
		for(Integer userId : participantsIDs) {
			User participant = SocialNetworkContext.getUserById(userId);
			participant.changeMoodByEvent(eventId, eventResult, false);
		}
	}
	
	private TypeOfResult calculateEventResult() {
		int chanceToFail = 0;
		for(Integer userId : participantsIDs) {
			User participant = SocialNetworkContext.getUserById(userId);
			if(RandomHelper.nextIntFromTo(0, 99) < participant.getCharacteristics().getRageRate() || !participant.isInMood()) {
				chanceToFail += (int) Math.ceil(100/participantsIDs.size());
			}
		}
		
		if(RandomHelper.nextIntFromTo(0, 99) < chanceToFail) {
			return TypeOfResult.NEGATIVE;
		} else {
			return TypeOfResult.POSITIVE;
		}
	}
	
	public void setStartingPopularity(int startingPopularity) {
		this.popularity = startingPopularity;
	}
	
	public void addTags(Tag... tags) {
		for(Tag tag : tags) {
			this.tags.add(tag);
		}
	}
	
	private void increasePopularity(int userId) {
		User newMember = SocialNetworkContext.getUserById(userId);
		popularity += newMember.getPageRankPoints()/10;
	}
	
	public void addUser(int userId) {
		if(active) {
			participantsIDs.add(userId);
			increasePopularity(userId);
			User newMember = SocialNetworkContext.getUserById(userId);
			User owner = SocialNetworkContext.getUserById(ownerID);
			owner.getPageRank().userJoinedMyEvent(newMember.getPageRankPoints());
		}
	}
	
	public int calculateValueToUser(User user) {
		int numberOfCommonTags = CollectionUtils.intersection(tags, user.getFavouriteTags()).size();
		int valueToUser = popularity + 50*numberOfCommonTags;
		
		int numberOfFriendsInEvent = CollectionUtils.intersection(participantsIDs, user.getFriends()).size();
		valueToUser += 10*numberOfFriendsInEvent;
		
		int numberOfRelativesInEvent = CollectionUtils.intersection(participantsIDs, user.getRelatives()).size();
		valueToUser += 15*numberOfRelativesInEvent;
		
		return valueToUser;
	}
	
}