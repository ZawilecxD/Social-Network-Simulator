package posts;

import java.util.ArrayList;
import java.util.HashSet;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.collections.CollectionUtils;

import network.SocialNetworkContext;
import network.Tag;
import network.TypeOfResult;
import user.User;

public class Post {
	
	private @Getter int postId;
	private @Getter int likesCount;
	
	private @Getter int ownerID;

	/**
	 * Popularnoœæ postu wp³ywa na chêæ u¿ytkowników do komentowania, lajkowania itd.
	 * Liczona na podstawie komentarzy i lajków, punkty otrzymane za lajk i komentarz zale¿¹ od pageranku usera.
	 */
	
	private @Getter @Setter int popularity;
	
	
	private @Getter ArrayList<Tag> tags;
	
	
	private @Getter ArrayList<Integer> commentersIDs;
	
	
	public Post(int ownerId) {
		this.postId = SocialNetworkContext.getNextPostId();
		this.ownerID = ownerId;
		this.popularity = 0;
		this.tags = new ArrayList<Tag>();
		this.commentersIDs = new ArrayList<Integer>();
	}
	
	public int calculateValueToUser(User user) {
		int numberOfCommonTags = CollectionUtils.intersection(tags, user.getFavouriteTags()).size();
		int valueToUser = popularity + 50*numberOfCommonTags;
		if(user.getFriends().contains(ownerID)) {
			valueToUser = (int) Math.ceil(valueToUser * 1.5);
		} else if(user.getRelatives().contains(ownerID)) {
			valueToUser *= 2;
		}
		return valueToUser;
	}
	
	public void likeIt(int likerPageRankPoints) {
		likesCount++;
		User owner = SocialNetworkContext.getUserById(ownerID);
		owner.getPageRank().receivedLike(likerPageRankPoints);
	}
	
	public void commentIt(int commenterId, TypeOfResult result) {
		commentersIDs.add(commenterId);
		User commentTarget = SocialNetworkContext.getUserById(ownerID);
		User commenter = SocialNetworkContext.getUserById(commenterId);
		commentTarget.changeMoodByAnswerToComment(commenterId, result, true);
		commentTarget.getPageRank().receivedComment(result, commenter.getPageRankPoints());
	}
	
	public void answerToComment(int answerAuthorId, int targetedCommentAuthorId, TypeOfResult result) {
		commentersIDs.add(answerAuthorId);
		User answerTarget = SocialNetworkContext.getUserById(targetedCommentAuthorId);
		User postOwner = SocialNetworkContext.getUserById(ownerID);
		User answerAuthor = SocialNetworkContext.getUserById(answerAuthorId);
		answerTarget.changeMoodByAnswerToComment(answerAuthorId, result, false);
		answerTarget.getPageRank().receivedComment(result, answerAuthor.getPageRankPoints());
		postOwner.getPageRank().receivedComment(result, answerAuthor.getPageRankPoints());
	}
	
	public void addTags(Tag... tags) {
		for(Tag tag : tags) {
			this.tags.add(tag);
		}
	}
	


}
