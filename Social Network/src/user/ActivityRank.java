package user;

import lombok.Getter;

public class ActivityRank {
	private @Getter int likesGiven;
	private @Getter int commentsGiven;
	private @Getter int negativCommentsGiven;
	private @Getter int postsCreated;
	private @Getter int groupsCreated;
	private @Getter int eventsCreated;
	private @Getter int successfulEvents;
	
	
	public ActivityRank() {
		this.likesGiven = 0;
		this.commentsGiven = 0;
		this.negativCommentsGiven = 0;
		this.postsCreated = 0;
		this.groupsCreated = 0;
		this.eventsCreated = 0;
		this.successfulEvents = 0;
	}
}
