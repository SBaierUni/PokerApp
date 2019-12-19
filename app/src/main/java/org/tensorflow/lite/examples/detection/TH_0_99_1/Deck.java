package org.tensorflow.lite.examples.detection.TH_0_99_1;

import java.util.ArrayList;
import java.util.Random;

public class Deck extends CardSet {
	private ArrayList<Integer> alreadyDrawnIndices;
	private long prevSeed= System.nanoTime();
	public Deck() {
		super("deck", 52);
		for (Suit s : Suit.values()) {
			for (Rank r: Rank.values()) {
				addCard(new Card(s, r));
			}
		}
		reset();
	}
	public Deck(ArrayList<Integer> alreadyDrawnInd) {
		super("deck", 52);
		for (Suit s : Suit.values()) {
			for (Rank r: Rank.values()) {
				addCard(new Card(s, r));
			}
		}
		alreadyDrawnIndices= (ArrayList<Integer>) (alreadyDrawnInd.clone());
	}
	public void reset() {
		alreadyDrawnIndices= new ArrayList<Integer>();
	}
	public ArrayList<Card> draw(int noOfCards) {
		ArrayList<Card> drawnCards= new ArrayList<Card>();
		long seed= System.nanoTime();
		while (prevSeed == seed)
			seed= System.nanoTime();
		prevSeed= seed;
		long fac= 1734856 + (long)(Math.random()*58743957);
		Random randNo = new Random(fac);
		for (int i= 0; i < noOfCards; i++) {
			int index = randNo.nextInt(52);  // index is a random number between 0 and 51
			while (alreadyDrawnIndices.contains(index)) {
				index = randNo.nextInt(52);
			}
			drawnCards.add(cards[index]);
			//System.out.print(cards[index]);
			//System.out.println("   " + index);
			alreadyDrawnIndices.add(index);
		}
		//System.out.println(alreadyDrawnIndices);
		return drawnCards;
	}
	public ArrayList<Integer> getAlreadyDrawnIndices() {
		return alreadyDrawnIndices;
	}
	public void addToAlreadyDrawnCards(CardSet cards) {
		for(Card c : cards.getCards()) {
			alreadyDrawnIndices.add(findIndexForCard(c));
		}
	}
	public void addToAlreadyDrawnCards(Card card) {
	    alreadyDrawnIndices.add(findIndexForCard(card));
    }
	public int findIndexForCard(Card c) {
		for(int i = 0; i < 52; i++) {
			if(cards[i].equals(c)) {
				return i;
			}
		}
		return -1;
	}
	public boolean isAlreadyDrawn(Card c) {
		for (Integer i: alreadyDrawnIndices) {
			if (findIndexForCard(c) == i.intValue()) {
				return true;
			}
		}
		return false;
	}
}
