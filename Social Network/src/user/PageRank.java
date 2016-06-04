package user;

import network.TypeOfResult;
import lombok.Getter;

public class PageRank {
	private @Getter int points;
	private @Getter int receivedLikes;
	private @Getter int receivedComments;
	private @Getter int receivedNegetiveComments;
	private @Getter int receivedEventCompetitors;
	private @Getter int receivedGroupsMembers;
	private @Getter int succeededEvents;
	
	public PageRank() {
		this.points = 1;
		this.receivedLikes = 0;
		this.receivedComments = 0;
		this.receivedNegetiveComments = 0;
		this.receivedEventCompetitors = 0;
		this.receivedGroupsMembers = 0;
	}
	
	public void addPoints(int anotherUserPageRankPoints) {
		if(anotherUserPageRankPoints > points) {
			points += (int) Math.ceil(anotherUserPageRankPoints/points) * 10;
		} else {
			points += 5;
		}
	}
	
	public void receivedComment(TypeOfResult commentResult, int commenterPageRankPoints) {
		receivedComments++;
		if(commentResult == TypeOfResult.NEGATIVE) {
			receivedNegetiveComments++;
		} else {
			addPoints(commenterPageRankPoints);
		}
	}
	
	public void receivedLike(int likerPageRankPoints) {
		receivedLikes++;
		addPoints(likerPageRankPoints);
	}
	
	public void eventSucceeded(int eventPopularity) {
		succeededEvents++;
		points += (int) Math.ceil(eventPopularity/100);
	}
	
	public void userJoinedMyEvent(int joinerPageRankPoints) {
		receivedEventCompetitors++;
		addPoints(joinerPageRankPoints);
	}
	
	public void userJoinedMyGroup(int joinerPageRankPoints) {
		receivedGroupsMembers++;
		addPoints(joinerPageRankPoints);
	}
}
