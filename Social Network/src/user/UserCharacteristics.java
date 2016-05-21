package user;

import lombok.Getter;
import lombok.Setter;

public class UserCharacteristics {
	private @Getter @Setter int meetNewFriendsRate; //defines a chance (1%-100%) that this user will find a friend
	private @Getter @Setter int likeRate; //defines a chance (1%-100%) to like
	private @Getter @Setter int commentRate; //defines a chance (1%-100%)  to comment
	private @Getter @Setter int postRate; //defines a chance (1%-100%)  to post 
	private @Getter @Setter int chatRate; //defines a chance (1%-100%)  to chat 
	private @Getter @Setter int groupBelongRate;
	private @Getter @Setter int eventParticipationRate;
	private @Getter @Setter int assertiveness;
	private @Getter @Setter int boardScrollRate; // useless??
	
	//how resistable is this person to negative interactions with others
	//(the lower value the bigger chance positive stats will fall after negative reaction of others)
	private @Getter @Setter int toughness; 
	
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
		
		return characteristics;
	}
	
}
