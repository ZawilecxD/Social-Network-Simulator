package user;

import network.TypeOfResult;
import lombok.Getter;

public class ActivityRank {
	private @Getter int points;
	private @Getter int likesGiven;
	private @Getter int commentsGiven;
	private @Getter int negativCommentsGiven;
	private @Getter int postsCreated;
	private @Getter int groupsCreated;
	private @Getter int eventsCreated;
	private @Getter int successfulEvents;
	
	
	public ActivityRank() {
		this.points = 0;
		this.likesGiven = 0;
		this.commentsGiven = 0;
		this.negativCommentsGiven = 0;
		this.postsCreated = 0;
		this.groupsCreated = 0;
		this.eventsCreated = 0;
		this.successfulEvents = 0;
	}
	
	public void addPoints(int value) {
		points += value;
	}
	
	public void gaveLike() {
		addPoints(1);
		likesGiven++;
	}
	
	public void gaveComment(TypeOfResult resultOfComment) {
		addPoints(2);
		commentsGiven++;
		if(resultOfComment == TypeOfResult.NEGATIVE) {
			negativCommentsGiven++;
		} 
	}
	
	public void createdPost() {
		addPoints(5);
		postsCreated++;
	}
	
	public void createdGroup() {
		addPoints(50);
		groupsCreated++;
	}
	
	public void createdEvent() {
		addPoints(20);
		eventsCreated++;
	}
	
	public void eventEndUpSuccess() {
		addPoints(100);
		successfulEvents++;
	}
	
}
