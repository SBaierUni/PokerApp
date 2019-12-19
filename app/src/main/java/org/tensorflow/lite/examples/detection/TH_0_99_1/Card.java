package org.tensorflow.lite.examples.detection.TH_0_99_1;

public class Card {
	private Suit suit;
	private Rank rank;
	private boolean isCorrect;
	private String cardAsShortString="";
	public Card(Suit s, Rank r) {
		suit= s;
		rank= r;
		isCorrect= true;
	}
	public Card(String cardDesc) {  // short cut for card descriptor
		//   eg, "D2"= DIAMOND-TWO
		//   "HA"= HEART-ACE
		//   "ST"= SPADE-TEN
		//   "CK"= CLUB-KING
		cardDesc = cardDesc.toUpperCase();  // just in case it was specified in lower-case letters
		if (cardDesc.length() != 2) {
			System.out.println("wrong card description, need a 2-character string");
			isCorrect= false;
			suit= Suit.HEART;
			rank= Rank.ACE;
			return;
		}
		isCorrect= true;
		switch (cardDesc.charAt(0)) {
			case 'C': suit= Suit.CLUB; break;
			case 'D': suit= Suit.DIAMOND; break;
			case 'H': suit= Suit.HEART; break;
			case 'S': suit= Suit.SPADE; break;
			default:
				System.out.println("wrong character for Suit: " + cardDesc.charAt(0));
				isCorrect= false;
				suit= Suit.HEART;
		}
		switch (cardDesc.charAt(1)) {
			case '2': rank= Rank.TWO; break;
			case '3': rank= Rank.THREE; break;
			case '4': rank= Rank.FOUR; break;
			case '5': rank= Rank.FIVE; break;
			case '6': rank= Rank.SIX; break;
			case '7': rank= Rank.SEVEN; break;
			case '8': rank= Rank.EIGHT; break;
			case '9': rank= Rank.NINE; break;
			case 'T': rank= Rank.TEN; break;
			case 'J': rank= Rank.JACK; break;
			case 'Q': rank= Rank.QUEEN; break;
			case 'K': rank= Rank.KING; break;
			case 'A': rank= Rank.ACE; break;
			default:
				System.out.println("wrong character for Rank: " + cardDesc.charAt(1));
				isCorrect= false;
				rank= Rank.ACE;
		}
		if (isCorrect) {
			cardAsShortString= cardDesc;
		}
	}
	public void display() {
		System.out.println(toString());
	}
	public String toString() {
		return "    " + suit + "-" + rank;
	}
	public Suit getSuit() {
		return suit;
	}
	public Rank getRank() {
		return rank;
	}
	public boolean isCorrect() { return isCorrect; }
	public String getCardAsShortString() {
		if (!cardAsShortString.isEmpty())
			return cardAsShortString;
		String cAsShortString="";
		switch (suit) {
			case CLUB: cAsShortString += "C"; break;
			case DIAMOND: cAsShortString += "D"; break;
			case HEART: cAsShortString += "H"; break;
			case SPADE: cAsShortString += "S"; break;
		}
		switch (rank) {
			case TWO: cAsShortString += "2"; break;
			case THREE: cAsShortString += "3"; break;
			case FOUR: cAsShortString += "4"; break;
			case FIVE: cAsShortString += "5"; break;
			case SIX: cAsShortString += "6"; break;
			case SEVEN: cAsShortString += "7"; break;
			case EIGHT: cAsShortString += "8"; break;
			case NINE: cAsShortString += "9"; break;
			case TEN: cAsShortString += "T"; break;
			case JACK: cAsShortString += "J"; break;
			case QUEEN: cAsShortString += "Q"; break;
			case KING: cAsShortString += "K"; break;
			case ACE: cAsShortString += "A"; break;
		}
		cardAsShortString= cAsShortString;
		return cAsShortString;
	}
	public boolean equals(Card otherCard){
		return this.suit == otherCard.suit && this.rank == otherCard.rank;
	}
}
