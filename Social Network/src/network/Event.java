package network;

import java.util.HashSet;
import java.util.List;

import user.User;

public class Event {

	public int eventId;
	public List<User> participants;
	public HashSet<Tag> tags;
	public int popularity;
	public User owner;
	
}