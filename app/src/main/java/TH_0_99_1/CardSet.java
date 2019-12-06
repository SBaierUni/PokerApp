package TH_0_99_1;

import java.util.ArrayList;

public class CardSet {
	protected Card cards[];
	private String cardSetName;
	protected int maxNoOfCards;
	protected int currCardIndex; // 0..maxNoOfCards-1
	public CardSet(String name, int maxNo) {
		cardSetName= name;
		maxNoOfCards= maxNo;
		cards= new Card[maxNoOfCards];
		currCardIndex= -1;
	}
	public CardSet(String cardsAsString, String name) {  // assumption: used for initializing hands
		String cardsDescriptions[]= cardsAsString.split(" ");  // shortest version!
		cardSetName= name;
		maxNoOfCards= 5;
		cards= new Card[maxNoOfCards];
		currCardIndex= -1;
		if (cardsDescriptions.length != 5) {
			System.out.println("5 cards expected => wrong number of cards specified!!");
			return;
		}
		for (int i= 0; i < 5; i= i + 1)	{
			addCard(new Card(cardsDescriptions[i]));
		}
	}
	public void display() {
		System.out.println(toString());
// original implementation:
//		System.out.println(cardSetName + ":");
//		for (int i=0; i<=currCardIndex; i= i+1) {
//			cards[i].display();
//		}
	}
	public String toString() {
		String result= "\n" + cardSetName + ":\n";
		for (int i= 0; i <= currCardIndex; i++) {
			result += "    "+ cards[i] + "\n";
		}
		return result;
	}
	public ArrayList<Card> getCards() {
		ArrayList<Card> allCards= new ArrayList<Card>();;
		for (int i= 0; i <= currCardIndex; i++) {
			allCards.add(cards[i]);
		}
		return allCards;
	}
	public void addCard(Card c) {
		if (currCardIndex >= maxNoOfCards - 1) {
			System.out.println("ERROR: " + cardSetName + " already full");
			return;
		}
		currCardIndex= currCardIndex + 1;
		cards[currCardIndex]= c;
	}
	public void addCards(ArrayList<Card> cardList) {
		if (currCardIndex + cardList.size() > maxNoOfCards - 1) {
			System.out.println("ERROR: " + cardSetName + "'s capacity too small");
			return;
		}
		for (Card c: cardList) {
			addCard(c);
		}
	}
	public Card cardAt(int i) {
		return cards[i];
	}
	public int getNoOfCards() {
		return currCardIndex + 1;
	}
	public boolean contains(Card c) {
		for (int i= 0; i <= currCardIndex; i++) {
			if (cards[i].equals(c)) {
				return true;
			}
		}
		return false;
	}
}