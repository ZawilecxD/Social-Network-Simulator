package user;

import lombok.Getter;
import lombok.Setter;

public class UserCharacteristics {
	public String name;
	
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
		characteristics.name = "Default";
		characteristics.setAssertiveness(10);
		characteristics.setAverageDailySessionLength(80);
		characteristics.setCommentRate(10);
		characteristics.setDailyMood(50);
		characteristics.setChatRate(10);
		characteristics.setEventParticipationRate(10);
		characteristics.setGroupBelongRate(10);
		characteristics.setLikeRate(10);
		characteristics.setMeetNewFriendsRate(10);
		characteristics.setPostRate(10);
		characteristics.setRageRate(10);
		characteristics.setEventHostingRate(10);
		characteristics.setGroupHostingRate(50);
		return characteristics;
	}
	
	public static UserCharacteristics chatterCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.name = "Chatter";
		characteristics.setAssertiveness(11);
		characteristics.setAverageDailySessionLength(80);
		characteristics.setCommentRate(12);
		characteristics.setDailyMood(40);
		characteristics.setChatRate(18);
		characteristics.setEventParticipationRate(7);
		characteristics.setGroupBelongRate(9);
		characteristics.setLikeRate(4);
		characteristics.setMeetNewFriendsRate(14);
		characteristics.setPostRate(2);
		characteristics.setRageRate(2);
		characteristics.setEventHostingRate(6);
		characteristics.setGroupHostingRate(3);
		return characteristics;
	}
	
	public static UserCharacteristics commentatorCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.name = "Commentator";
		characteristics.setAssertiveness(4);
		characteristics.setAverageDailySessionLength(55);
		characteristics.setCommentRate(18);
		characteristics.setDailyMood(30);
		characteristics.setChatRate(14);
		characteristics.setEventParticipationRate(4);
		characteristics.setGroupBelongRate(12);
		characteristics.setLikeRate(7);
		characteristics.setMeetNewFriendsRate(6);
		characteristics.setPostRate(10);
		characteristics.setRageRate(9);
		characteristics.setEventHostingRate(2);
		characteristics.setGroupHostingRate(2);
		return characteristics;
	}
	
	public static UserCharacteristics likerCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.name = "Liker";
		characteristics.setAssertiveness(14);
		characteristics.setAverageDailySessionLength(35);
		characteristics.setCommentRate(5);
		characteristics.setDailyMood(50);
		characteristics.setChatRate(4);
		characteristics.setEventParticipationRate(3);
		characteristics.setGroupBelongRate(9);
		characteristics.setLikeRate(14);
		characteristics.setMeetNewFriendsRate(10);
		characteristics.setPostRate(6);
		characteristics.setRageRate(2);
		characteristics.setEventHostingRate(1);
		characteristics.setGroupHostingRate(3);
		return characteristics;
	}
	
	public static UserCharacteristics groupieCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.name = "Groupie";
		characteristics.setAssertiveness(16);
		characteristics.setAverageDailySessionLength(60);
		characteristics.setCommentRate(14);
		characteristics.setDailyMood(30);
		characteristics.setChatRate(12);
		characteristics.setEventParticipationRate(16);
		characteristics.setGroupBelongRate(15);
		characteristics.setLikeRate(13);
		characteristics.setMeetNewFriendsRate(16);
		characteristics.setPostRate(14);
		characteristics.setRageRate(3);
		characteristics.setEventHostingRate(12);
		characteristics.setGroupHostingRate(16);
		return characteristics;
	}
	
	public static UserCharacteristics lonerCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.name = "Loner";
		characteristics.setAssertiveness(1);
		characteristics.setAverageDailySessionLength(25);
		characteristics.setCommentRate(2);
		characteristics.setDailyMood(10);
		characteristics.setChatRate(3);
		characteristics.setEventParticipationRate(1);
		characteristics.setGroupBelongRate(2);
		characteristics.setLikeRate(8);
		characteristics.setMeetNewFriendsRate(4);
		characteristics.setPostRate(4);
		characteristics.setRageRate(2);
		characteristics.setEventHostingRate(2);
		characteristics.setGroupHostingRate(2);
		return characteristics;
	}
	
	public static UserCharacteristics nervousCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.name = "Nervous";
		characteristics.setAssertiveness(2);
		characteristics.setAverageDailySessionLength(47);
		characteristics.setCommentRate(11);
		characteristics.setDailyMood(10);
		characteristics.setChatRate(6);
		characteristics.setEventParticipationRate(3);
		characteristics.setGroupBelongRate(2);
		characteristics.setLikeRate(6);
		characteristics.setMeetNewFriendsRate(6);
		characteristics.setPostRate(4);
		characteristics.setRageRate(16);
		characteristics.setEventHostingRate(3);
		characteristics.setGroupHostingRate(3);
		return characteristics;
	}
	
	public static UserCharacteristics haterCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.name = "Hater";
		characteristics.setAssertiveness(1);
		characteristics.setAverageDailySessionLength(58);
		characteristics.setCommentRate(16);
		characteristics.setDailyMood(4);
		characteristics.setChatRate(13);
		characteristics.setEventParticipationRate(2);
		characteristics.setGroupBelongRate(12);
		characteristics.setLikeRate(1);
		characteristics.setMeetNewFriendsRate(2);
		characteristics.setPostRate(14);
		characteristics.setRageRate(35);
		characteristics.setEventHostingRate(1);
		characteristics.setGroupHostingRate(6);
		return characteristics;
	}
	
	public static UserCharacteristics nerdCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.name = "Nerd";
		characteristics.setAssertiveness(1);
		characteristics.setAverageDailySessionLength(45);
		characteristics.setCommentRate(10);
		characteristics.setDailyMood(10);
		characteristics.setChatRate(12);
		characteristics.setEventParticipationRate(1);
		characteristics.setGroupBelongRate(18);
		characteristics.setLikeRate(9);
		characteristics.setMeetNewFriendsRate(2);
		characteristics.setPostRate(9);
		characteristics.setRageRate(5);
		characteristics.setEventHostingRate(2);
		characteristics.setGroupHostingRate(9);
		return characteristics;
	}
	
	public static UserCharacteristics observerCharacteristics() {
		UserCharacteristics characteristics = new UserCharacteristics();
		characteristics.name = "Observer";
		characteristics.setAssertiveness(2);
		characteristics.setAverageDailySessionLength(27);
		characteristics.setCommentRate(2);
		characteristics.setDailyMood(19);
		characteristics.setChatRate(1);
		characteristics.setEventParticipationRate(1);
		characteristics.setGroupBelongRate(6);
		characteristics.setLikeRate(1);
		characteristics.setMeetNewFriendsRate(1);
		characteristics.setPostRate(1);
		characteristics.setRageRate(2);
		characteristics.setEventHostingRate(1);
		characteristics.setGroupHostingRate(4);
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
