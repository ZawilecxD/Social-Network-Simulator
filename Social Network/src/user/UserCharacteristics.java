package user;

import lombok.Getter;
import lombok.Setter;

public class UserCharacteristics {
	private @Getter @Setter int meetNewFriendsRate; //defines a chance (1%-100%) that this user will find a friend
	private @Getter @Setter int likeRate; //defines a chance (1%-100%) to like
	private @Getter @Setter int commentRate; //defines a chance (1%-100%)  to comment
	private @Getter @Setter int postRate; //defines a chance (1%-100%)  to post 
	private @Getter @Setter int chatRate; //defines a chance (1%-100%)  to chat 
	private @Getter @Setter int groupBelongRate;//defines a chance (1%-100%)  to join group 
	private @Getter @Setter int groupHostingRate;//defines a chance (1%-100%)  to create group 
	private @Getter @Setter int eventParticipationRate;//defines a chance (1%-100%)  to join event
	private @Getter @Setter int eventHostingRate;//defines a chance (1%-100%)  to create event 
	private @Getter @Setter int assertiveness; //defines a chance (1-100%) to gain tag during chat
	
	
	//how vulnerable is this person to negative interactions with others
	//(the higher value the bigger chance positive stats will fall after negative reaction of others or failed events)
	private @Getter @Setter int vulnerability; 
	
	private @Getter @Setter int rageRate; //0-100 
	private @Getter @Setter int dailyMood; //0-100 chance to gain starting mood (at login) that is 5 or greater
	private @Getter @Setter int averageDailySessionLength; //average number of tickes user spends every day on fb , maximum is 1440minutes (24 hours)
	
	public UserCharacteristics() {
		
	}

	public static UserCharacteristics defaultCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.setAssertiveness(10);
		characteristics.setAverageDailySessionLength(100);
		characteristics.setCommentRate(50);
		characteristics.setDailyMood(5);
		characteristics.setChatRate(50);
		characteristics.setEventParticipationRate(50);
		characteristics.setGroupBelongRate(50);
		characteristics.setLikeRate(50);
		characteristics.setMeetNewFriendsRate(50);
		characteristics.setPostRate(50);
		characteristics.setRageRate(10);
		characteristics.setEventHostingRate(50);
		characteristics.setGroupHostingRate(50);
		return characteristics;
	}
	
	public void changeAverageMood(int value) {
		dailyMood += value;
	}
	
	public void changeEventParticipationRate(int value) {
		eventParticipationRate += value;
	}
	
	public void changeChatRate(int value) {
		chatRate += value;
	}
	
	public void changeCommentRate(int value) {
		commentRate += value;
	}
	
	public void changePostRate(int value) {
		postRate += value;
	}
	
	public void changeEventHostingRate(int value) {
		eventHostingRate += value;
	}
	
}
