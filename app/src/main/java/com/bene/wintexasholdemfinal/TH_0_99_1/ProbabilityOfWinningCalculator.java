package com.bene.wintexasholdemfinal.TH_0_99_1;
import java.util.ArrayList;

public class ProbabilityOfWinningCalculator {
	private ArrayList<Integer> alreadyDrawnIndices;
	private PlayerPocket playerPocket;
	private ArrayList<Card> talonCards;
	private int numberOfOpponents;

	public ProbabilityOfWinningCalculator(Talon t, PlayerPocket pp, Deck d, int noOfOpponents) {
		alreadyDrawnIndices = d.getAlreadyDrawnIndices();
		playerPocket= pp;
		talonCards = t.getCards();
		numberOfOpponents= noOfOpponents;
	}
	public double calcProbability(int sampleSize) {
		int handsWon = 0;
		for(int i = 0; i < sampleSize; i++) {
			Deck deck= new Deck(alreadyDrawnIndices);
			Talon tempTalon = new Talon(talonCards);
			tempTalon.addCards(deck.draw(5 - talonCards.size()));
			Player me = new Player(tempTalon, playerPocket);
			Hand myHighestHand = me.getHighestHand();
			boolean iWin = true;
			for(int j = 0; j < numberOfOpponents; j++) {
				PlayerPocket opponentPocket = new PlayerPocket(deck.draw(2));
				Player opponent = new Player(tempTalon, opponentPocket);
				if(myHighestHand.compareTo(opponent.getHighestHand()) < 0) {
					iWin = false;
					break;  // leave for-loop
				}
			}
			if(iWin) {
				handsWon++;
			}
		}
		return (100.0 * handsWon)/sampleSize;
	}
}