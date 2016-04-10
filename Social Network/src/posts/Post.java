package posts;

import user.User;

public class Post {
	private User owner;
	private PostType postType;
	private String text;
	
	
	public Post(User owner, PostType type, String text) {
		this.owner = owner;
		this.postType = type;
		this.text = text;
	}
	
	public User getOwner() {
		return owner;
	}


	public void setOwner(User owner) {
		this.owner = owner;
	}


	public PostType getPostType() {
		return postType;
	}


	public void setPostType(PostType postType) {
		this.postType = postType;
	}


	public String getText() {
		return text;
	}


	public void setText(String text) {
		this.text = text;
	}


}
