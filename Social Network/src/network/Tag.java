package network;

public enum Tag {
	TASTY(1),
	CANDY_CRUSH_SAGA(2),
	CRISTIANIO_RONALDO(3),
	FACEBOOK(4),
	FOOTBALL(5),
	MUSIC(6),
	SHAKIRA(7),
	VIN_DIESEL(8),
	BASKETBALL(9),
	CIFRAS(10),
	MR_BEAN(11),
	FAST_AND_FURIOUS(12),
	YOUTUBE(13),
	DISNEY(14),
	MTV(15),
	NINE_GAG(16),
	JESUS_DAILY(17),
	HARRY_POTTER(18),
	TITANIC(19),
	COCA_COLA(20),
	MC_DONALDS(21),
	PIZZA(22),
	MICROSOFT(23),
	EMINEM(24),
	LEO_MESSI(25),
	RIHANNA(26),
	JUSTIN_BIEBER(27),
	RED_BULL(28),
	KFC(29),
	PLAY_STATION(30),
	WILL_SMITH(31),
	HISTORY(32),
	DISCOVERY(33),
	INSTAGRAM(34);
	
	private int popularity;
	
	private Tag(int value) {
	    this.popularity = value;
	}

	public int getValue() {
		return popularity;
	}
	
}
