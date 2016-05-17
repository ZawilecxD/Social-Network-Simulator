package user;

import lombok.Getter;

public class PageRank {
	private @Getter int points;
	private @Getter int achievedLikes;
	private @Getter int achievedComments;
	private @Getter int achievedEventCompetitors;
	private @Getter int achievedGroupsMembers;
	
	public PageRank() {
		this.points = 0;
		this.achievedLikes = 0;
		this.achievedComments = 0;
		this.achievedEventCompetitors = 0;
		this.achievedGroupsMembers = 0;
		
	}
}
