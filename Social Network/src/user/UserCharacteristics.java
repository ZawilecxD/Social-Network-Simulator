package user;

import lombok.Getter;
import lombok.Setter;

public class UserCharacteristics {
	private @Getter @Setter int meetNewFriendsRate; //defines a chance (1%-100%) that this user will find a friend
	private @Getter @Setter int likeRate; //defines a chance (1%-100%) to like
	private @Getter @Setter int commentRate; //defines a chance (1%-100%)  to comment
	private @Getter @Setter int postRate; //defines a chance (1%-100%)  to post 
	private @Getter @Setter double groupBelongRate;
	private @Getter @Setter double eventParticipationRate;
	private @Getter @Setter double assertiveness;
	private @Getter @Setter int boardScrollRate; // useless??
	private @Getter @Setter double amorousness;
	private @Getter @Setter int rageRate; //0-100 
	private @Getter @Setter int dailyMood; //0-100 chance to gain starting mood (at login) that is 5 or greater
	private @Getter @Setter int averageDailySessionLength; //average number of tickes user spends every day on fb 
	
	public UserCharacteristics() {
		
	}

	public static UserCharacteristics defaultCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.setAmorousness(1.0);
		characteristics.setAssertiveness(1.0);
		characteristics.setAverageDailySessionLength(50);
		characteristics.setCommentRate(1);
		characteristics.setDailyMood(5);
		characteristics.setEventParticipationRate(1.0);
		characteristics.setGroupBelongRate(1);
		characteristics.setLikeRate(1);
		characteristics.setMeetNewFriendsRate(1);
		characteristics.setPostRate(1);
		characteristics.setRageRate(1);
		
		return characteristics;
	}
	
}
