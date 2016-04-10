package user;

import java.util.HashMap;
import java.util.List;

import repast.simphony.engine.schedule.ScheduledMethod;
import network.Event;
import network.Group;

public class User {
	
	private int userId;
	private String name;
	private String surname;
	private Sex sex;
	
	/**
	 * Cechy charakteru definiuj¹ce danego user'a i jego chêæ do wykonywania ró¿nych akcji
	 */
	private UserCharacteristics characteristics;
	
	/**
	 * Ustatalany ka¿dorazowo przy zalogowaniu do servisu, powiedzmy ¿e ma wartoœci miêdzy 1-10, 
	 * gdzie 10 to super nastawienie i chêæ kontaktu z innymi userami
	 */
	private double mood; 
	
	/**
	 * Collection of friends, userId -> User object
	 */
	private HashMap<Integer, User> friends;
	
	/**
	 * Collection of groups this user is part of, groupId -> Group object
	 */
	private HashMap<Integer, Group> groups;
	
	/**
	 * Collection of events this user attends, eventId -> Event object
	 */
	private HashMap<Integer, Event> events;
	
	private User relationshipPartner;
	
	public User(int id, String name, String surname, Sex sex,UserCharacteristics character) {
		this.userId = id;
		this.name = name;
		this.surname = surname;
		this.sex = sex;
		this.characteristics = character;
	}
	
	@ScheduledMethod(start = 1, interval = 10)
	public void sessionActions() {
		
	}
	
	public void addFriend(){
		
	}
	
	public void addPost(){
		
	}
	
	public void addPhoto(){
		
	}
	
	public void commentPost(){
		
	}
	
	public void commentPhoto(){
		
	}
	
	public void likePost(){
		
	}
	
	public void likePhoto(){
		
	}
	
	public void removeFriend(){
		
	}
	
	public void chatWithFriend(){
		
	}
	
	public void chatWithFriends(){
		
	}
	
	public void createGroup(){
		
	}
	
	public void createEvent(){
		
	}
	
	public void joinGroup(){
		
	}
	
	public void joinEvent(){
		
	}
	
	public void deleteGroup(){
		
	}
	
	public void deleteEvent(){
		
	}
	
	public void makeRelationship(){
		
	}

	public HashMap<Integer, User> getFriends() {
		return friends;
	}

	public void setFriends(HashMap<Integer, User> friends) {
		this.friends = friends;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public Sex getSex() {
		return sex;
	}

	public void setSex(Sex sex) {
		this.sex = sex;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UserCharacteristics getCharacteristics() {
		return characteristics;
	}

	public void setCharacteristics(UserCharacteristics characteristics) {
		this.characteristics = characteristics;
	}

	public double getMood() {
		return mood;
	}

	public void setMood(double mood) {
		this.mood = mood;
	}

	public HashMap<Integer, Group> getGroups() {
		return groups;
	}

	public void setGroups(HashMap<Integer, Group> groups) {
		this.groups = groups;
	}

	public HashMap<Integer, Event> getEvents() {
		return events;
	}

	public void setEvents(HashMap<Integer, Event> events) {
		this.events = events;
	}

	public User getRelationshipPartner() {
		return relationshipPartner;
	}

	public void setRelationshipPartner(User relationshipPartner) {
		this.relationshipPartner = relationshipPartner;
	}

}